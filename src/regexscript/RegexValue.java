package regexscript;

import java.util.regex.Pattern;

public final class RegexValue implements Value {
  public final String pattern;
  public final String flags;
  public final Pattern compiled;

  public RegexValue(String pattern, String flags) {
    this.pattern = pattern;
    this.flags = flags == null ? "" : flags;

    int f = 0;
    if (this.flags.contains("i")) f |= Pattern.CASE_INSENSITIVE;
    if (this.flags.contains("m")) f |= Pattern.MULTILINE;

    this.compiled = Pattern.compile(pattern, f);
  }

  @Override public String typeName() { return "Regex"; }
  @Override public Object unwrap() { return compiled; }
}
