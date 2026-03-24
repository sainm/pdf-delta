package org.sainm.ocr.paddle;

import org.sainm.model.BoundingBox;
import org.sainm.ocr.paddle.model.OcrResult;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;






public final class PostProcessor {

    private static final float DB_THRESHOLD = 0.3f;
    private static final float DB_BOX_THRESHOLD = 0.5f;
    private static final float DB_UNCLIP_RATIO = 1.5f;

    private final List<String> dictionary;

    public PostProcessor(List<String> dictionary) {
        this.dictionary = dictionary;
    }

    int dictionarySize() {
        return dictionary.size();
    }

    



    public static PostProcessor fromDictFile(Path dictPath) throws IOException {
        var chars = Files.readAllLines(dictPath, StandardCharsets.UTF_8);
        var dict = new ArrayList<String>(chars.size() + 1);
        dict.add(""); 
        dict.addAll(chars);
        return new PostProcessor(dict);
    }

    


    public static PostProcessor defaultChinese() {
        var dict = new ArrayList<String>();
        dict.add(""); 
        try (var reader = new BufferedReader(new InputStreamReader(
                PostProcessor.class.getResourceAsStream("/ppocr_keys_v1.txt"),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dict.add(line);
            }
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Failed to load built-in dictionary", e);
        }
        return new PostProcessor(dict);
    }

    public static PostProcessor defaultJapanese() {
        return fromClasspath("/japan_dict.txt");
    }

    private static PostProcessor fromClasspath(String resource) {
        var dict = new ArrayList<String>();
        dict.add(""); 
        try (var reader = new BufferedReader(new InputStreamReader(
                PostProcessor.class.getResourceAsStream(resource),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dict.add(line);
            }
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Failed to load built-in dictionary: " + resource, e);
        }
        return new PostProcessor(dict);
    }

    

    







    public List<float[][]> dbPostProcess(float[] output, int outputH, int outputW, float ratio) {
        List<float[][]> boxes = new ArrayList<>();

        
        boolean[][] mask = new boolean[outputH][outputW];
        for (int y = 0; y < outputH; y++) {
            for (int x = 0; x < outputW; x++) {
                mask[y][x] = output[y * outputW + x] > DB_THRESHOLD;
            }
        }

        boolean[][] visited = new boolean[outputH][outputW];
        for (int y = 1; y < outputH - 1; y++) {
            for (int x = 1; x < outputW - 1; x++) {
                if (mask[y][x] && !visited[y][x]) {
                    var region = floodFill(mask, visited, x, y, outputW, outputH);
                    if (region.area < 10) continue;

                    float score = regionScore(output, outputW, region);
                    if (score < DB_BOX_THRESHOLD) continue;

                    float[][] box = regionToBox(region, ratio);
                    if (box != null) boxes.add(box);
                }
            }
        }
        return boxes;
    }

    

    






    public OcrResult ctcDecode(float[] output, int seqLen, int vocabSize) {
        var sb = new StringBuilder();
        float totalScore = 0;
        int validCount = 0;
        int lastIdx = 0;

        for (int t = 0; t < seqLen; t++) {
            int bestIdx = 0;
            float bestVal = output[t * vocabSize];
            for (int v = 1; v < vocabSize; v++) {
                float val = output[t * vocabSize + v];
                if (val > bestVal) {
                    bestVal = val;
                    bestIdx = v;
                }
            }
            
            if (bestIdx != 0 && bestIdx != lastIdx) {
                if (bestIdx < dictionary.size()) {
                    sb.append(dictionary.get(bestIdx));
                }
                totalScore += bestVal;
                validCount++;
            }
            lastIdx = bestIdx;
        }

        float avgScore = validCount > 0 ? totalScore / validCount : 0;
        return new OcrResult(sb.toString(), null, avgScore);
    }

    


    public static BufferedImage cropRegion(BufferedImage source, float[][] box) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (float[] pt : box) {
            minX = Math.min(minX, pt[0]);
            minY = Math.min(minY, pt[1]);
            maxX = Math.max(maxX, pt[0]);
            maxY = Math.max(maxY, pt[1]);
        }
        int x = Math.max(0, (int) minX);
        int y = Math.max(0, (int) minY);
        int w = Math.min((int) Math.ceil(maxX) - x, source.getWidth() - x);
        int h = Math.min((int) Math.ceil(maxY) - y, source.getHeight() - y);
        if (w <= 0 || h <= 0) return new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        return source.getSubimage(x, y, w, h);
    }

    


    public static BoundingBox toBoundingBox(float[][] box, int page) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (float[] pt : box) {
            minX = Math.min(minX, pt[0]);
            minY = Math.min(minY, pt[1]);
            maxX = Math.max(maxX, pt[0]);
            maxY = Math.max(maxY, pt[1]);
        }
        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        return new BoundingBox(minX, minY, Math.max(0, maxX - minX), Math.max(0, maxY - minY), page);
    }

    

    private static Region floodFill(boolean[][] mask, boolean[][] visited,
                                     int startX, int startY, int w, int h) {
        int minX = startX, maxX = startX, minY = startY, maxY = startY;
        int area = 0;
        var stack = new ArrayList<int[]>();
        stack.add(new int[]{startX, startY});
        visited[startY][startX] = true;

        while (!stack.isEmpty()) {
            int[] pt = stack.remove(stack.size() - 1);
            int px = pt[0], py = pt[1];
            area++;
            minX = Math.min(minX, px);
            maxX = Math.max(maxX, px);
            minY = Math.min(minY, py);
            maxY = Math.max(maxY, py);

            for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int nx = px + d[0], ny = py + d[1];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h
                        && mask[ny][nx] && !visited[ny][nx]) {
                    visited[ny][nx] = true;
                    stack.add(new int[]{nx, ny});
                }
            }
        }
        return new Region(minX, minY, maxX, maxY, area);
    }

    private static float regionScore(float[] output, int outputW, Region region) {
        float sum = 0;
        int count = 0;
        for (int y = region.minY; y <= region.maxY; y++) {
            for (int x = region.minX; x <= region.maxX; x++) {
                sum += output[y * outputW + x];
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }

    private static float[][] regionToBox(Region region, float ratio) {
        
        float cx = (region.minX + region.maxX) / 2.0f;
        float cy = (region.minY + region.maxY) / 2.0f;
        float hw = (region.maxX - region.minX) / 2.0f * DB_UNCLIP_RATIO;
        float hh = (region.maxY - region.minY) / 2.0f * DB_UNCLIP_RATIO;

        
        float x1 = (cx - hw) / ratio;
        float y1 = (cy - hh) / ratio;
        float x2 = (cx + hw) / ratio;
        float y2 = (cy + hh) / ratio;

        if (x2 - x1 < 3 || y2 - y1 < 3) return null;

        return new float[][]{
                {x1, y1}, {x2, y1}, {x2, y2}, {x1, y2}
        };
    }

    private record Region(int minX, int minY, int maxX, int maxY, int area) {}
}
