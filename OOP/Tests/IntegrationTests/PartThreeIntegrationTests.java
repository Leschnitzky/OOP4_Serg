package OOP.Tests.IntegrationTests;

import OOP.Provided.OOPExpectedException;
import OOP.Solution.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class PartThreeIntegrationTests {

    @OOPTestClass
    public static class FirstIntTest{
        public static int test = 0;
        public static int num_of_test_methods = 0;
        public static int num_of_before_methods = 0;
        public static int num_of_after_methods = 0;
        public static boolean before_cons;
        public static boolean after_cons;
        private boolean before = false;
        private boolean after = false;
        private boolean first_bool_before = false;
        private boolean second_bool_before = false;
        private boolean first_bool_after = false;
        private boolean second_bool_after = false;

        @OOPSetup
        public void first_setUp(){
            test++;
        }

        @OOPSetup
        public void second_SetUP(){
            test++;
        }

        public void third_setUP(){
            test++;
        }

        @OOPTest(order = 1, tag = "yes")
        public void first_test(){
            after = true;
            first_bool_after = true;
            num_of_test_methods+=4;
            if(first_bool_before && second_bool_before){
                before_cons = true;
            }

            first_bool_before = false;
            second_bool_before = false;
        }

        @OOPTest(order = 2)
        public void second_test(){
            num_of_test_methods++;
            before = true;
        }

        @OOPTest(order = 3, tag = "yes")
        public void fourth_test(){
            num_of_test_methods++;
            second_bool_after = true;
            if(first_bool_before && second_bool_before){
                before_cons = true;
            }

            first_bool_before = false;
            second_bool_before = false;
        }

        @OOPAfter(value = {"first_test"})
        public void firstAfter(){
            num_of_after_methods++;
            if(!after){
                num_of_after_methods += 500;
            }

            if(second_bool_after && first_bool_after){
                after_cons = true;
            }

            second_bool_after = false;
            first_bool_after = false;
        }
        @OOPAfter(value = {"second_test","fourth_test"})
        public void secondAfter(){
            num_of_after_methods++;

            if(second_bool_after && first_bool_after){
                after_cons = true;
            }

            second_bool_after = false;
            first_bool_after = false;
        }


        @OOPBefore(value = {"second_test","fourth_test"})
        public void firstBefore(){
            first_bool_before = true;
            num_of_before_methods++;
            if(!before){
                num_of_before_methods -= 256;
            }
        }

        @OOPBefore(value = {"first_test"})
        public void secondBefore(){
            second_bool_before = true;
            num_of_before_methods++;
        }

    }

    @OOPTestClass
    public static class SecondIntTest extends FirstIntTest{


        @OOPSetup
        public void first_setUp(){
            test+= 3;
        }
        @OOPSetup
        public void fourth_setUP(){
            test++;
        }

        @OOPTest(order = 4,tag = "yes")
        public void third_test(){
            num_of_test_methods++;
        }

        @OOPTest(order = 5, tag = "yes")
        public void first_test(){
            num_of_test_methods++;
        }

        @OOPTest(order = 6)
        public void fourth_test(){
            num_of_test_methods++;
        }

        @OOPBefore(value = {"first_test"})
        public void firstBefore(){
            num_of_before_methods++;
        }


        @OOPBefore(value = {"second_test"})
        public void secondBefore(){
            num_of_before_methods++;
        }

        @OOPBefore(value = {"fourth_test"})
        public void thirdBefore(){
            num_of_before_methods++;
        }

        @OOPAfter(value = {"first_test"})
        public void firstAfter(){
            num_of_after_methods++;
        }


        @OOPAfter(value = {"second_test"})
        public void secondAfter(){
            num_of_after_methods++;
        }

        @OOPAfter(value = {"fourth_test"})
        public void thirdAfter(){
            num_of_after_methods++;
        }


    }

    @OOPTestClass
    public static class ThirdIntTest{
        @OOPExceptionRule
        public OOPExpectedException mExpectedException = OOPExpectedException.none();

        @OOPTest(order = 1, tag = "test1")
        public void passingTest(){}

        @OOPTest(order = 2, tag = "test2")
        public void failingTest(){
            OOPUnitCore.fail();
        }

        @OOPTest(order = 3, tag = "test3")
        public void errorTest() throws IOException {
            throw new IOException();
        }

        @OOPTest(order = 4, tag = "test4")
        public void caughtExceptionTest() throws IOException {
            mExpectedException.expect(IOException.class);
            mExpectedException.expectMessage("correct message");
            throw new IOException("correct message");
        }

        @OOPTest(order = 5, tag = "test5")
        public void caughtIncorrect() throws IOException {
            mExpectedException.expect(IOException.class);
            mExpectedException.expectMessage("incorrect message");
            throw new IOException("correct message");
        }
    }

    @Before
    public void setUp(){
        FirstIntTest.num_of_test_methods = 0;
        FirstIntTest.num_of_before_methods = 0;
        FirstIntTest.num_of_after_methods = 0;
        FirstIntTest.test = 0;

    }

    @Test
    public void testRunningAllSetupMethods(){
        OOPUnitCore.runClass(FirstIntTest.class);
        assertEquals(FirstIntTest.test,2);
    }

    @Test
    public void testRunningAllSetupMethodsInheritance(){
        OOPUnitCore.runClass(SecondIntTest.class);
        assertEquals(FirstIntTest.test,5);
    }

    @Test
    public void testRunningTestMethodsWithoutTag(){
        OOPUnitCore.runClass(FirstIntTest.class);
        assertEquals(FirstIntTest.num_of_test_methods,1);
    }

    @Test
    public void testRunningTestMethodsWithTag(){
        OOPUnitCore.runClass(FirstIntTest.class, "yes");
        assertEquals(FirstIntTest.num_of_test_methods,5);
    }

    @Test
    public void testRunningTestMethodsWithInheritanceNoTag(){
        OOPUnitCore.runClass(SecondIntTest.class);
        assertEquals(FirstIntTest.num_of_test_methods,2);
    }

    @Test
    public void testRunningTestMethodsWithInheritanceWithTag(){
        OOPUnitCore.runClass(SecondIntTest.class,"yes");
        assertEquals(FirstIntTest.num_of_test_methods,2);
    }

    @Test
    public void testRunningBeforeMethodsNoTag(){
        OOPUnitCore.runClass(FirstIntTest.class);
        assertEquals(FirstIntTest.num_of_before_methods,-255);
    }

    @Test
    public void testRunningBeforeBeforeTest(){
        OOPUnitCore.runClass(FirstIntTest.class);
        assertEquals(FirstIntTest.num_of_before_methods,-255);
    }

    @Test
    public void testRunningBeforeMethodsWithTag(){
        OOPUnitCore.runClass(FirstIntTest.class,"yes");
        assertEquals(FirstIntTest.num_of_before_methods,-254);
    }

    @Test
    public void testRunningBeforeMethodsNoTagInheritance(){
        OOPUnitCore.runClass(SecondIntTest.class);
        assertEquals(FirstIntTest.num_of_before_methods,2);
    }

    @Test
    public void testRunningBeforeMethodsWithTagInheritance(){
        OOPUnitCore.runClass(SecondIntTest.class,"yes");
        assertEquals(FirstIntTest.num_of_before_methods,1);
    }

    @Test
    public void testRunningBeforeTestsConsecutive(){
        OOPUnitCore.runClass(FirstIntTest.class,"yes");
        assertTrue(FirstIntTest.before_cons);
    }


    @Test
    public void testRunningAfterMethodsAfterTest(){
        OOPUnitCore.runClass(FirstIntTest.class,"yes");
        assertEquals(FirstIntTest.num_of_after_methods,2);
    }

    @Test
    public void testConsecutiveAfterMethods(){
        OOPUnitCore.runClass(FirstIntTest.class,"yes");
        assertTrue(FirstIntTest.after_cons);
    }

    @Test
    public void testRunningAfterMethodsNoTag(){
        OOPUnitCore.runClass(FirstIntTest.class);
        assertEquals(FirstIntTest.num_of_after_methods,1);
    }


    @Test
    public void testRunningAfterMethodsWithTag(){
        OOPUnitCore.runClass(FirstIntTest.class,"yes");
        assertEquals(FirstIntTest.num_of_after_methods,2);
    }

    @Test
    public void testRunningAfterMethodsNoTagInheritance(){
        OOPUnitCore.runClass(SecondIntTest.class);
        assertEquals(FirstIntTest.num_of_before_methods,2);
    }

    @Test
    public void testRunningAfterMethodsWithTagInheritance(){
        OOPUnitCore.runClass(SecondIntTest.class,"yes");
        assertEquals(FirstIntTest.num_of_before_methods,1);
    }


    @Test
    public void testASimplePassingTest(){
        OOPTestSummary testSummary = OOPUnitCore.runClass(ThirdIntTest.class,"test1");
        assertEquals(testSummary.getNumSuccesses(), 1);
    }

    @Test
    public void testASimpleFailingTest(){
        OOPTestSummary testSummary = OOPUnitCore.runClass(ThirdIntTest.class,"test2");
        assertEquals(testSummary.getNumFailures(), 1);
    }

    @Test
    public void testASimpleErrorTest(){
        OOPTestSummary testSummary = OOPUnitCore.runClass(ThirdIntTest.class,"test3");
        assertEquals(testSummary.getNumErrors(), 1);
    }

    @Test
    public void testASimpleCoughtExpectedExceptionWithCorrectMessage(){
        OOPTestSummary testSummary = OOPUnitCore.runClass(ThirdIntTest.class,"test4");
        assertEquals(testSummary.getNumSuccesses(), 1);
    }

    @Test
    public void testASimpleCaughtExpectedExceptionWithIncorrectMessage(){
        OOPTestSummary testSummary = OOPUnitCore.runClass(ThirdIntTest.class,"test5");
        assertEquals(testSummary.getNumExceptionMismatches(), 1);
    }










}
