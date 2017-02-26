package neutrality;

/**
 * This object gets translated directly into column headers and data
 * values in output files. For this reason, it needs to be flattened and
 * not hierarchical. Also, it will necessarily be somewhat repetitive
 * because fields from other object cannot be re-used directly.
 * <p>
 * Notes to self:
 * <p>
 * (1) Use first class types to distinguish between null and default
 * values. (2) Don't add values here faster than writing the code that
 * calculates them, this helps to make sure nothing gets missed!  ;)
 */
public class OutputData {
/*
Consumption Variables
 */
double utilityVideoOnly;
double utilityOtherOnly;
double utilityBoth;

/*
Network Operator Variables
 */
double nspQtyNetworkOnly; // Unbundled Network
double nspRevNetworkOnly;
double nspQtyVideoOnly; // Unbundled Content
double nspRevVideoOnly;
double nspQtyBundle; // Bundled network and content
double nspRevBundle;
double nspQtyIxcVideo; // IXC from Video
double nspRevIxcVideo;
double nspQtyIxcOther; // IXC from Other
double nspRevIxcOther;
double nspKn; // Investment
double nspKa;
double nspBalance; // Balance/Fitness

/*
Content Provider Variables
 */
double vcpQty;
double vcpRev;
double vcpKa;
double vcpBalance;

double ocpQty;
double ocpRev;
double ocpKa;
double ocpBalance;

/*
Market variables
 */
double hhiNetwork;
double hhiVideo;
double hhiOther;
int    nspBankruptcies;
int    vcpBankruptcies;
int    ocpBankruptcies;

@Override
public String toString() {
  return "OutputData{" +
         "\nutilityVideoOnly=" + utilityVideoOnly +
         ", \nutilityOtherOnly=" + utilityOtherOnly +
         ", \nutilityBoth=" + utilityBoth +
         ", \nnspQtyNetworkOnly=" + nspQtyNetworkOnly +
         ", \nnspRevNetworkOnly=" + nspRevNetworkOnly +
         ", \nnspQtyVideoOnly=" + nspQtyVideoOnly +
         ", \nnspRevVideoOnly=" + nspRevVideoOnly +
         ", \nnspQtyBundle=" + nspQtyBundle +
         ", \nnspRevBundle=" + nspRevBundle +
         ", \nnspQtyIxcVideo=" + nspQtyIxcVideo +
         ", \nnspRevIxcVideo=" + nspRevIxcVideo +
         ", \nnspQtyIxcOther=" + nspQtyIxcOther +
         ", \nnspRevIxcOther=" + nspRevIxcOther +
         ", \nnspKn=" + nspKn +
         ", \nnspKa=" + nspKa +
         ", \nnspBalance=" + nspBalance +
         ", \nvcpQty=" + vcpQty +
         ", \nvcpRev=" + vcpRev +
         ", \nvcpKa=" + vcpKa +
         ", \nvcpBalance=" + vcpBalance +
         ", \nocpQty=" + ocpQty +
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
