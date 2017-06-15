package neutrality;

import java.util.Optional;

/**
 * This
 *
 * @author kkoning
 */
public abstract class ConsumptionOption {

public NetworkOperator network;
public Optional<ContentProvider> video;
public Optional<ContentProvider> other;
public boolean zeroRated;

public double Kn() {
  return network.Kn;
}

public double videoKa() {
  if (video.isPresent())
    return video.get().Ka;
  else
    return 0d;
}

public double otherKa() {
  if (other.isPresent())
    return other.get().Ka;
  else
    return 0d;
}

public abstract double getTotalCost();
public abstract void consume(Consumers consumers, double qty);


}
