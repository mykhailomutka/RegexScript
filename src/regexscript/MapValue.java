package regexscript;

import java.util.HashMap;
import java.util.Map;

public final class MapValue implements Value {
  public final Map<String, Value> map;
  public MapValue() { this.map = new HashMap<>(); }
  public MapValue(Map<String, Value> map) { this.map = map; }
  @Override public String typeName() { return "Map"; }
  @Override public Object unwrap() { return map; }
}
