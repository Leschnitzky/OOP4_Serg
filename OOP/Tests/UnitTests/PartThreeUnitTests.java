package OOP.Tests.UnitTests;

import OOP.Provided.OOPAssertionFailure;
import OOP.Solution.OOPUnitCore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static junit.framework.TestCase.fail;

public class PartThreeUnitTests {

    OOPUnitCore tUnitCore;

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Test
    public void testFail(){
        mExpectedException.expect(OOPAssertionFailure.class);
        OOPUnitCore.fail();
    }

    @Test
    public void testAssertEqualsUnequal(){
        mExpectedException.expect(OOPAssertionFailure.class);
        String s = "yes";
        String t = "no";

        OOPUnitCore.assertEquals(s,t);
    }

    @Test
    public void testAssertEqualsEqual(){
        String s = "yes";
        String t = "yes";

        try{
            OOPUnitCore.assertEquals(s,t);
        } catch(Exception e){
            fail();
        }
    }



}
