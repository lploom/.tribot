package scripts.utils;

import java.util.ArrayList;

public class FixedSizedList<T> extends ArrayList<T> {
  
  private static final long serialVersionUID = 1L;
  private int maxSize;

  public FixedSizedList(int size) {
    this.maxSize = size;
  }

  @Override
  public boolean add(T e) {
    while(super.size() >= maxSize) {
      super.remove(0);
    }
    return super.add(e);
  }
}
