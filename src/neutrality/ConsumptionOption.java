package neutrality;

public class ConsumptionOption {

	NetworkOperator<?>	network;
	ContentProvider<?>	videoContent;
	ContentProvider<?>	otherContent;
	double				price;

	// if this option is consumed, these agents get paid this much.
	double				toNetwork;
	double				toVideoContent;
	double				toOtherContent;

	public void consume() {
		// TODO: Stub
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("K_n=" + network.getInvestment() + ",");
		if (videoContent != null) {
			sb.append("K_{a,vid}=" + videoContent.getInvestment() + ",");
			sb.append("Pref_{a,vid}=" + videoContent.preference + ",");
		}
		if (otherContent != null) {
			sb.append("K_{a,oth}=" + otherContent.getInvestment() + ",");
			sb.append("Pref_{a,oth}=" + otherContent.preference + ",");
		}
		sb.append("p=" + price);

		return sb.toString();
	}

}
