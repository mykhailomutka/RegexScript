package regexscript;

import java.util.ArrayList;
import java.util.List;

public final class ListValue implements Value {
  public final List<Value> items;
  public ListValue() { this.items = new ArrayList<>(); }
  public ListValue(List<Value> items) { this.items = items; }
  @Override public String typeName() { return "List"; }
  @Override public Object unwrap() { return items; }
}
