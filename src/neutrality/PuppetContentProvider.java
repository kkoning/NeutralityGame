package neutrality;

import agency.NullIndividual;
import neutrality.Offers.ContentOffer;

/**
 * To be controlled by a network provider.
 * 
 * @author kkoning
 *
 */
public class PuppetContentProvider extends ContentProvider<NullIndividual> {

	public PuppetContentProvider() {
		isVideoProvider = true;
	}
	
	@Override
	public ContentOffer getContentOffer() {
		throw new RuntimeException("Should not be called");
	}

	@Override
	public void step() {
		throw new RuntimeException("Should not be called");
	}

}
