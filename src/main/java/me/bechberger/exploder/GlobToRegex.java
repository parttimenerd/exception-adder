package me.bechberger.exploder;

public class GlobToRegex {
    static String toRegex(String glob) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '\\' && i + 1 < glob.length()) {
                // Add literal backslash for regex
                sb.append("\\\\");
                char next = glob.charAt(++i);
                // Escape regex metacharacters after the backslash
                if ("\\.[]{}()*+-?^$|".indexOf(next) >= 0) {
                    sb.append("\\").append(next);
                } else {
                    sb.append(next);
                }
            } else {
                switch (c) {
                    case '*':
                        if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                            sb.append(".*");
                            i++;
                        } else {
                            sb.append("[^.]*");
                        }
                        break;
                    case '?':
                        sb.append('.');
                        break;
                    case '.':
                        sb.append("\\.");
                        break;
                    default:
                        sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}