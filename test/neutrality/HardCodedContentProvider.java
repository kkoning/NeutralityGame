package neutrality;

import agency.NullIndividual;
import neutrality.Offers.ContentOffer;

public class HardCodedContentProvider extends ContentProvider<NullIndividual> {

	ContentOffer fixedOffer;
	
	/**
	 * Default constructor, all prices and investment have unit values (=1).
	 */
	public HardCodedContentProvider(NeutralityModel model, boolean isVideoProvider) {
		super(model, isVideoProvider);
		
		contentInvestment = 1;
		this.fixedOffer = new ContentOffer(this, 1);
	}

	@Override
	public void step() {
		// Do nothing; behavior is fixed over steps.
	}

	@Override
	public ContentOffer getContentOffer() {
		return fixedOffer;
	}

}
