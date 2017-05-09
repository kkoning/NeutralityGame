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
double utilityOtherOnly;
double utilityBoth;

/*
 * Network Operator Variables
 */
double nspPriceNetworkOnly; // Unbundled Network
double nspRevNetworkOnly;
double nspPriceVideoOnly;   // Unbundled Content
double nspRevVideoOnly;
double nspPriceBundle;      // Bundled network and content
double nspRevBundle;
double nspQtyIxcVideo;    // IXC from Video
double nspRevIxcVideo;
double nspQtyIxcOther;    // IXC from Other
double nspRevIxcOther;
double nspKn;             // Investment
double nspKa;
double nspBalance;        // Balance/Fitness

/*
 * Content Provider Variables
 */
double vcpP;
double vcpRev;
double vcpKa;
double vcpBalance;

double ocpP;
double ocpRev;
double ocpKa;
double ocpBalance;

/*
 * Market variables
 */
double hhiNetwork;
double hhiVideo;
double hhiOther;
int    nspBankruptcies;
int    vcpBankruptcies;
int    ocpBankruptcies;

public void checkForNaNs() {
  if (Double.isNaN(utilityVideoOnly) ||
      Double.isNaN(utilityOtherOnly) ||
      Double.isNaN(utilityBoth) ||
      Double.isNaN(nspPriceNetworkOnly) ||
      Double.isNaN(nspRevNetworkOnly) ||
      Double.isNaN(nspPriceVideoOnly) ||
      Double.isNaN(nspRevVideoOnly) ||
      Double.isNaN(nspPriceBundle) ||
      Double.isNaN(nspQtyIxcVideo) ||
      Double.isNaN(nspRevIxcVideo) ||
      Double.isNaN(nspQtyIxcOther) ||
      Double.isNaN(nspRevIxcOther) ||
      Double.isNaN(nspKn) ||
      Double.isNaN(nspKa) ||
      Double.isNaN(nspBalance) ||
      Double.isNaN(nspKn) ||
      Double.isNaN(nspKa) ||
      Double.isNaN(nspBalance) ||
      Double.isNaN(vcpP) ||
      Double.isNaN(vcpRev) ||
      Double.isNaN(vcpKa) ||
      Double.isNaN(vcpBalance) ||
      Double.isNaN(ocpP) ||
      Double.isNaN(ocpRev) ||
      Double.isNaN(ocpKa) ||
      Double.isNaN(ocpBalance) ||
      Double.isNaN(hhiNetwork) ||
      Double.isNaN(hhiVideo) ||
      Double.isNaN(hhiOther) ||
      Double.isNaN(hhiVideo))
    throw new RuntimeException();
}

public void checkForInfinities() {
  if (Double.isInfinite(utilityVideoOnly) ||
      Double.isInfinite(utilityOtherOnly) ||
      Double.isInfinite(utilityBoth) ||
      Double.isInfinite(nspPriceNetworkOnly) ||
      Double.isInfinite(nspRevNetworkOnly) ||
      Double.isInfinite(nspPriceVideoOnly) ||
      Double.isInfinite(nspRevVideoOnly) ||
      Double.isInfinite(nspPriceBundle) ||
      Double.isInfinite(nspQtyIxcVideo) ||
      Double.isInfinite(nspRevIxcVideo) ||
      Double.isInfinite(nspQtyIxcOther) ||
      Double.isInfinite(nspRevIxcOther) ||
      Double.isInfinite(nspKn) ||
      Double.isInfinite(nspKa) ||
      Double.isInfinite(nspBalance) ||
      Double.isInfinite(nspKn) ||
      Double.isInfinite(nspKa) ||
      Double.isInfinite(nspBalance) ||
      Double.isInfinite(vcpP) ||
      Double.isInfinite(vcpRev) ||
      Double.isInfinite(vcpKa) ||
      Double.isInfinite(vcpBalance) ||
      Double.isInfinite(ocpP) ||
      Double.isInfinite(ocpRev) ||
      Double.isInfinite(ocpKa) ||
      Double.isInfinite(ocpBalance) ||
      Double.isInfinite(hhiNetwork) ||
      Double.isInfinite(hhiVideo) ||
      Double.isInfinite(hhiOther) ||
      Double.isInfinite(hhiVideo))
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
         ", \nnspBalance=" + nspBalance +
         ", \nvcpP=" + vcpP +
         ", \nvcpRev=" + vcpRev +
         ", \nvcpKa=" + vcpKa +
         ", \nvcpBalance=" + vcpBalance +
         ", \nocpP=" + ocpP +
         ", \nocpRev=" + ocpRev +
         ", \nocpKa=" + ocpKa +
         ", \nocpBalance=" + ocpBalance +
         ", \nhhiNetwork=" + hhiNetwork +
         ", \nhhiVideo=" + hhiVideo +
         ", \nhhiOther=" + hhiOther +
         ", \nnspBankruptcies=" + nspBankruptcies +
         ", \nvcpBankruptcies=" + vcpBankruptcies +
         ", \nocpBankruptcies=" + ocpBankruptcies +
         "\n}";
}

}
