package org.sainm.normalizer;

import org.sainm.model.CompareOptions;
import org.sainm.spi.Normalizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateNormalizer implements Normalizer {
    private static final Pattern SLASH = Pattern.compile("(\\d{4})/(\\d{1,2})/(\\d{1,2})");
    private static final Pattern CN    = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");

    @Override public int order() { return 40; }

    @Override
    public String normalize(String text, CompareOptions options) {
        if (!options.isIgnoreDateFormat()) return text;
        text = replaceDate(SLASH, text);
        text = replaceDate(CN, text);
        return text;
    }

    private String replaceDate(Pattern p, String text) {
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String yyyy = m.group(1);
            String mm   = String.format("%02d", Integer.parseInt(m.group(2)));
            String dd   = String.format("%02d", Integer.parseInt(m.group(3)));
            m.appendReplacement(sb, yyyy + "-" + mm + "-" + dd);
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
