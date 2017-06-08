package neutrality;

public class SalesTracker {
public double price = Double.NaN;
public double qty = 0d;

public double revenue() {
  return price * qty;
}

}
