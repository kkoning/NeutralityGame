package neutrality;

/**
 * This object gets translated directly into column headers and data values in
 * output files. For this reason, it needs to be flattened and not hierarchical.
 * Also, it will necessarily be somewhat repetitive because fields from other
 * object cannot be re-used directly.
 * <p>
 * Notes to self:
 * <p>
 * (1) Use first class types to distinguish between null and default values. (2)
 * Don't add values here faster than writing the code that calculates them, this
 * helps to make sure nothing gets missed! ;)
 */
public class OutputData {
/*
 * Consumption Variables
 */
double utilityVideoOnly;
double utilityPerCostVideoOnly;
double utilityOtherOnly;
double utilityPerCostOtherOnly;
double utilityBoth;
double utilityPerCostBoth;

/*
 * Network Operator Variables
 */
double nspPriceNetworkOnly; // Unbundled Network
double nspRevNetworkOnly;
double nspPriceVideoOnly;   // Unbundled Content
double nspRevVideoOnly;
double nspPriceBundle;      // Bundled network and content
double nspRevBundle;
double nspRevVideoBW;       // Bandwidth fees from consumers
double nspRevOtherBW;
double nspQtyIxcVideo;      // IXC from Video
double nspRevIxcVideo;
double nspQtyIxcOther;      // IXC from Other
double nspRevIxcOther;
double nspKn;               // Investment
double nspKa;
double nspProfit;           // Profit

/*
 * Content Provider Variables
 */
double vcpP;
double vcpRev;
double vcpKa;
double vcpProfit;

double ocpP;
double ocpRev;
double ocpKa;
double ocpProfit;

/*
 * Market variables
 */
double hhiNetwork;
double hhiVideo;
double hhiOther;

int nspBankruptcies;
int vcpBankruptcies;
int ocpBankruptcies;

public void checkForNaNs() {
  if (Double.isNaN(utilityVideoOnly))
    throw new RuntimeException();
  if (Double.isNaN(utilityBoth))
    throw new RuntimeException();
  if (Double.isNaN(nspPriceNetworkOnly))
    throw new RuntimeException();
  if (Double.isNaN(nspRevNetworkOnly))
    throw new RuntimeException();
  if (Double.isNaN(nspPriceVideoOnly))
    throw new RuntimeException();
  if (Double.isNaN(nspRevVideoOnly))
    throw new RuntimeException();
  if (Double.isNaN(nspPriceBundle))
    throw new RuntimeException();
  if (Double.isNaN(nspRevBundle))
    throw new RuntimeException();
  if (Double.isNaN(nspRevVideoBW))
    throw new RuntimeException();
  if (Double.isNaN(nspRevOtherBW))
    throw new RuntimeException();
  if (Double.isNaN(nspQtyIxcVideo))
    throw new RuntimeException();
  if (Double.isNaN(nspRevIxcVideo))
    throw new RuntimeException();
  if (Double.isNaN(nspQtyIxcOther))
    throw new RuntimeException();
  if (Double.isNaN(nspRevIxcOther))
    throw new RuntimeException();
  if (Double.isNaN(nspKn))
    throw new RuntimeException();
  if (Double.isNaN(nspKa))
    throw new RuntimeException();
  if (Double.isNaN(nspProfit))
    throw new RuntimeException();
  if (Double.isNaN(vcpP))
    throw new RuntimeException();
  if (Double.isNaN(vcpRev))
    throw new RuntimeException();
  if (Double.isNaN(vcpKa))
    throw new RuntimeException();
  if (Double.isNaN(vcpProfit))
    throw new RuntimeException();
  if (Double.isNaN(ocpP))
    throw new RuntimeException();
  if (Double.isNaN(utilityVideoOnly))
    throw new RuntimeException();
  if (Double.isNaN(ocpRev))
    throw new RuntimeException();
  if (Double.isNaN(ocpKa))
    throw new RuntimeException();
  if (Double.isNaN(ocpProfit))
    throw new RuntimeException();
  if (Double.isNaN(hhiNetwork))
    throw new RuntimeException();
  if (Double.isNaN(hhiVideo))
    throw new RuntimeException();
  if (Double.isNaN(hhiOther))
    throw new RuntimeException();
}

public void checkForInfinities() {
  if (Double.isInfinite(utilityVideoOnly))
    throw new RuntimeException();
  if (Double.isInfinite(utilityBoth))
    throw new RuntimeException();
  if (Double.isInfinite(nspPriceNetworkOnly))
    throw new RuntimeException();
  if (Double.isInfinite(nspRevNetworkOnly))
    throw new RuntimeException();
  if (Double.isInfinite(nspPriceVideoOnly))
    throw new RuntimeException();
  if (Double.isInfinite(nspRevVideoOnly))
    throw new RuntimeException();
  if (Double.isInfinite(nspPriceBundle))
    throw new RuntimeException();
  if (Double.isInfinite(nspRevBundle))
    throw new RuntimeException();
  if (Double.isInfinite(nspRevVideoBW))
    throw new RuntimeException();
  if (Double.isInfinite(nspRevOtherBW))
    throw new RuntimeException();
  if (Double.isInfinite(nspQtyIxcVideo))
    throw new RuntimeException();
  if (Double.isInfinite(nspRevIxcVideo))
    throw new RuntimeException();
  if (Double.isInfinite(nspQtyIxcOther))
    throw new RuntimeException();
  if (Double.isInfinite(nspRevIxcOther))
    throw new RuntimeException();
  if (Double.isInfinite(nspKn))
    throw new RuntimeException();
  if (Double.isInfinite(nspKa))
    throw new RuntimeException();
  if (Double.isInfinite(nspProfit))
    throw new RuntimeException();
  if (Double.isInfinite(vcpP))
    throw new RuntimeException();
  if (Double.isInfinite(vcpRev))
    throw new RuntimeException();
  if (Double.isInfinite(vcpKa))
    throw new RuntimeException();
  if (Double.isInfinite(vcpProfit))
    throw new RuntimeException();
  if (Double.isInfinite(ocpP))
    throw new RuntimeException();
  if (Double.isInfinite(utilityVideoOnly))
    throw new RuntimeException();
  if (Double.isInfinite(ocpRev))
    throw new RuntimeException();
  if (Double.isInfinite(ocpKa))
    throw new RuntimeException();
  if (Double.isInfinite(ocpProfit))
    throw new RuntimeException();
  if (Double.isInfinite(hhiNetwork))
    throw new RuntimeException();
  if (Double.isInfinite(hhiVideo))
    throw new RuntimeException();
  if (Double.isInfinite(hhiOther))
    throw new RuntimeException();
}

@Override
public String toString() {
  return "OutputData{" +
         "\nutilityVideoOnly=" + utilityVideoOnly +
         ", \nutilityOtherOnly=" + utilityOtherOnly +
         ", \nutilityBoth=" + utilityBoth +
         ", \nnspPriceNetworkOnly=" + nspPriceNetworkOnly +
         ", \nnspRevNetworkOnly=" + nspRevNetworkOnly +
         ", \nnspPriceVideoOnly=" + nspPriceVideoOnly +
         ", \nnspRevVideoOnly=" + nspRevVideoOnly +
         ", \nnspPriceBundle=" + nspPriceBundle +
         ", \nnspRevBundle=" + nspRevBundle +
         ", \nnspQtyIxcVideo=" + nspQtyIxcVideo +
         ", \nnspRevIxcVideo=" + nspRevIxcVideo +
         ", \nnspQtyIxcOther=" + nspQtyIxcOther +
         ", \nnspRevIxcOther=" + nspRevIxcOther +
         ", \nnspKn=" + nspKn +
         ", \nnspKa=" + nspKa +
         ", \nnspProfit=" + nspProfit +
         ", \nvcpP=" + vcpP +
         ", \nvcpRev=" + vcpRev +
         ", \nvcpKa=" + vcpKa +
         ", \nvcpProfit=" + vcpProfit +
         ", \nocpP=" + ocpP +
         ", \nocpRev=" + ocpRev +
         ", \nocpKa=" + ocpKa +
         ", \nocpProfit=" + ocpProfit +
         ", \nhhiNetwork=" + hhiNetwork +
         ", \nhhiVideo=" + hhiVideo +
         ", \nhhiOther=" + hhiOther +
         ", \nnspBankruptcies=" + nspBankruptcies +
         ", \nvcpBankruptcies=" + vcpBankruptcies +
         ", \nocpBankruptcies=" + ocpBankruptcies +
         "\n}";
}

}
