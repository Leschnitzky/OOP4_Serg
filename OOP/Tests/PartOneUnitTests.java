package OOP.Tests;

import OOP.Provided.OOPExpectedException;
import OOP.Solution.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;


public class PartOneUnitTests extends TestUtilityClass {

    @Mock
    OOPExpectedException mException;



    @OOPTestClass
    public class TestOne{

        @OOPExceptionRule
        public OOPExpectedException mExpectedException = OOPExpectedExceptionImpl.none();

        @OOPBefore(value = {"One","Two","Three"})
        public void beforeTest(){ }

        @OOPSetup
        public void setUp(){

        }
        @OOPAfter(value = {"One"})
        public void setAfter(){
        }

        @OOPTest(order = 1)
        public void testOne(){
        }

        @OOPTest(order = 2,tag = "Breakfast")
        public void testTwo(){
        }

    }

    private Annotation tTestClassAnnotation;
    private Annotation tExceptionRuleAnnotation;
    private Annotation tAfterAnnotation;
    private Annotation tTestAnnotation;
    private Annotation tTestTwoAnnotation;
    private Annotation tBeforeAnnotation;
    private Annotation tSetupAnnotation;

    @Before
    public void setUp(){
        mException = OOPExpectedException.none();
        try {
            tTestClassAnnotation = TestOne.class.getAnnotation(OOPTestClass.class);
            tExceptionRuleAnnotation =
                    TestOne.class.
                    getField("mExpectedException")
                            .getAnnotation(OOPExceptionRule.class);

            tAfterAnnotation = TestOne.class.
                    getDeclaredMethod("setAfter").getAnnotation(OOPAfter.class);
            tTestAnnotation = TestOne.class.
                    getDeclaredMethod("testOne").getAnnotation(OOPTest.class);
            tTestTwoAnnotation = TestOne.class.
                    getDeclaredMethod("testTwo").getAnnotation(OOPTest.class);
            tBeforeAnnotation = TestOne.class.
                    getDeclaredMethod("beforeTest").getAnnotation(OOPBefore.class);
            tSetupAnnotation = TestOne.class.
                    getDeclaredMethod("setUp").getAnnotation(OOPSetup.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTestClassAnotationRuntime(){
        assertNotNull(tTestClassAnnotation);
    }

    @Test
    public void testRuleRuntimeRetention(){
        assertNotNull(tExceptionRuleAnnotation);
    }

    @Test
    public void testAfterAnnotationRetention(){
        assertNotNull(tAfterAnnotation);
    }


    @Test
    public void testBeforeAnnotationRetention(){
        assertNotNull(tBeforeAnnotation);
    }

    @Test
    public void testSetupAnnotationRetention(){
        assertNotNull(tSetupAnnotation);
    }

    @Test
    public void testExpectedExpect(){
        mException.expect(IOException.class);
        assertEquals(mException.getExpectedException(),IOException.class);
    }

    @Test
    public void testGetEmptyExpectedException(){
        assertEquals(mException.getExpectedException(),null);
    }

    @Test
    public void testAssertExceptionInheritance(){
        mException.expect(Exception.class);
        IOException e = new IOException();
        assertTrue(mException.assertExpected(e));
    }

    @Test
    public void testAssertExceptionFather(){
        mException.expect(IOException.class);
        Exception e = new Exception();
        assertFalse(mException.assertExpected(e));
    }

    @Test
    public void testExpectedMultipleMessages(){
        mException.expect(Exception.class);
        try{
            mException.expectMessage("BBB").expectMessage("CCC");
        }
        catch (Exception e){
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testOOPTestClassDefaultValues() throws IllegalAccessException, InstantiationException {
        assertEquals(
                getAnnotationMember(tTestClassAnnotation,"value").toString()
                ,"UNORDERED");
    }

    @Test
    public void testOOPAfterSetValues() throws IllegalAccessException, InstantiationException {
        String[] test = {"One"};
        String[] actual = (String[]) getAnnotationMember(tAfterAnnotation,"value");
        assertTrue(test[0].equals(actual[0]));
    }

    @Test
    public void testOOPBeforeSetValues() throws IllegalAccessException, InstantiationException {
        String[] test = {"One","Two","Three"};
        String[] actual = (String[]) getAnnotationMember(tBeforeAnnotation,"value");
        assertEquals(test.length,actual.length);
    }

    @Test
    public void testOOPTestDefaultValues(){
        int firstValue = (int) getAnnotationMember(tTestAnnotation,"order");
        String secondValue = (String) getAnnotationMember(tTestAnnotation,"tag");

        assertEquals(firstValue,1);
        assertEquals(secondValue,"");
    }

    @Test
    public void testOOPTestSetValues(){
        int firstValue = (int) getAnnotationMember(tTestTwoAnnotation,"order");
        String secondValue = (String) getAnnotationMember(tTestTwoAnnotation,"tag");

        assertEquals(firstValue,2);
        assertEquals(secondValue,"Breakfast");
    }


}
