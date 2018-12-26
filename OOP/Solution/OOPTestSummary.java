package OOP.Solution;

import OOP.Provided.OOPResult;

import java.util.Map;
import java.util.stream.Collectors;

public class OOPTestSummary {

    private int mNumSuccesses;
    private int mNumFailures;
    private int mNumErrors;
    private int mNumExceptionMismatches;

    public OOPTestSummary(Map<String, OOPResult> testMap){
        mNumSuccesses = (int)testMap.values().stream()
                .filter(x -> x.getResultType() == OOPResult.OOPTestResult.SUCCESS)
                .count();
        mNumFailures = (int)testMap.values().stream()
                .filter(x -> x.getResultType() == OOPResult.OOPTestResult.FAILURE)
                .count();
        mNumErrors = (int)testMap.values().stream()
                .filter(x -> x.getResultType() == OOPResult.OOPTestResult.ERROR)
                .count();
        mNumExceptionMismatches = (int)testMap.values().stream()
                .filter(x -> x.getResultType() == OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH)
                .count();

    }
    public int getNumSuccesses() {
        return mNumSuccesses;
    }

    public int getNumFailures() {
        return mNumFailures;
    }

    public int getNumErrors() {
        return mNumErrors;
    }

    public int getNumExceptionMismatches() {
        return mNumExceptionMismatches;
    }
}
