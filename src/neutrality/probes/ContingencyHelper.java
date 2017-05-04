package neutrality.probes;

import agency.Agent;
import agency.vector.VectorIndividual;
import neutrality.NeutralityModel;

import java.util.Collections;
import java.util.List;

import static agency.util.Misc.BUG;

/**
 * Created by liara on 4/18/17.
 */
public class ContingencyHelper<T> {

final Agent<VectorIndividual<T>, NeutralityModel> agent;

final int                            genomeStartPosition;
final int                            outputMatrixStartPosition;
final List<EnvironmentalContingency> contingencies;
final List<Enum<?>>                  outputVariables;
final boolean[]                      contingencyTestResults;

Object[] genome;
int      outputVectorGenomeOffset = 0;

public ContingencyHelper(Agent<VectorIndividual<T>, NeutralityModel> agent,
                         int genomeStartPosition,
                         List<EnvironmentalContingency> contingencies,
                         List<Enum<?>> outputVariables) {

  this.agent = agent;
  this.contingencies = Collections.unmodifiableList(contingencies);
  this.outputVariables = Collections.unmodifiableList(outputVariables);
  this.contingencyTestResults = new boolean[contingencies.size()];

  this.genomeStartPosition = genomeStartPosition;
  this.outputMatrixStartPosition = genomeStartPosition + contingencies.size();

  // TODO: Sanity tests to make sure everything is OK.
  // (E.g., genome length is sufficient)

}

public static int intTranslate(boolean[] bits) {
  int result = 0;
  for (int i = 0; i < bits.length; i++) {
    int position = bits.length - 1 - i;
    if (bits[position]) {
      int mask = 0x00000001 << i;
      result = result | mask;
    }
  }
  return result;
}

/**
 * Tests the environment and updates the objects in currentGenomeValues, which
 * will be what is returned when getGenomeValueFor is called.
 */
public void update() {
  // the genome won't be available at constructor time, but we'll want to
  // keep a reference instead of going through a bunch of functions every time.
  if (genome == null)
    genome = agent.getManager().getGenome();

  NeutralityModel model = agent.getModel();

  // Make sure we're not being called in step 0
  if (model.currentStep == 0)
    BUG("ContingencyHelper.update() cannot be called in model step 0");

  // Test each one of the contingencies, record results.
  for (int i = 0; i < contingencies.size(); i++) {
    EnvironmentalContingency ec = contingencies.get(i);
    Number parameter = null;
    try {
      parameter = (Number) genome[genomeStartPosition + i];
    } catch (ClassCastException cce) {
      BUG("Individual is assuming a numberic genome type, but actual data " +
          "type cannot be cast to java.lang.Number");
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      BUG("Attempt to access a parameter past the end of a genome");
    }

    contingencyTestResults[i] = ec.conditionMet(
        model,
        agent,
        // Always look back exactly 1 step
        model.getMarketInformation(model.currentStep - 1),
        parameter);
  }

  // Translate the contingencies into an index of the output vector within
  // the output matrix.
  int outputVectorIndex = intTranslate(contingencyTestResults);
  outputVectorGenomeOffset = genomeStartPosition + contingencies.size() +
      (outputVectorIndex * outputVariables.size());

  // We should now be done. outputVectorGenomeOffset plus the output enum
  // will provide us with the unique location on the genome to return.
}

public T getGenomeValueFor(Enum<?> outVar) {
  int outScalarIndex = outVar.ordinal();
  if (outScalarIndex > outputVariables.size())
    BUG("Attempt to get output value with enum.ordinal() = " + outScalarIndex
        + ", but there were only " + outputVariables.size() + " output " +
        "variables in the list at the creation of this ContingencyHelper");

  return getGenomeValueForOffset(outScalarIndex);
}

private T getGenomeValueForOffset(int offset) {
  int genomePos = outputVectorGenomeOffset + offset;
  T toReturn = agent.getManager().gene(genomePos);
  return toReturn;
}

public int getGenomeLengthUsed() {
  int length = 0;
  length += contingencies.size(); // One loci for each parameter
  length += (0x00000001 << contingencies.size()) * outputVariables.size();
  return length;
}

/**
 * Assumes the genome represents the coefficients of a linear equation. The
 * first loci is assumed to be a constant, and is used as e^loci. The remaining
 * loci are assumed to be coefficients fo the arguments in x_n.
 * <p>
 * x_n.length must be equal to outputVariables.size()-1
 */
public double applyLinearEq(Number[] x_n) {
  if (x_n.length != outputVariables.size() - 1)
    BUG("Mismatching size of genome section and environment info");

  double constantTerm = ((Number) getGenomeValueForOffset(0)).doubleValue();
  double[] coefficients = new double[outputVariables.size() - 1];
  for (int i = 0; i < coefficients.length; i++) {
    Number genomeVal = (Number) getGenomeValueForOffset(i + 1);
    coefficients[i] = genomeVal.doubleValue();
  }

  double toReturn = Math.exp(constantTerm);
  for (int i = 0; i < coefficients.length; i++) {
    double product = x_n[i].doubleValue() * coefficients[i];
    toReturn += product;
  }

  return toReturn;
}

public int nextAvailableLoci() {
  return genomeStartPosition + getGenomeLengthUsed();
}

}
