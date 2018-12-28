package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExceptionMismatchError;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;
import OOP.Tests.ExampleTest;
import OOP.Tests.IntegrationTests.PartThreeIntegrationTests;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class OOPUnitCore {

    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<>();

        for (T t : list1) {
            if (list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

    public static OOPTestSummary runClass(Class<?> exampleClassClass) {
        return runClass(exampleClassClass,"");
    }

    public static void assertEquals(Object first, Object second) {
        if(!first.equals(second)){
            throw new OOPAssertionFailure();
        }
    }

    public static void fail() {
        throw new OOPAssertionFailure();
    }

    public static OOPTestSummary runClass(Class<?> classInstance, String tag) {
        Object instance = null; // Creates a new instance
        HashMap<String,OOPResult> testResults = new HashMap<>();
        try {
            instance = classInstance.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Invoke all setup methods

        Object finalInstance = instance;
        Arrays.stream(classInstance.getMethods()).filter((x)-> (x.getAnnotation(OOPSetup.class) != null)
        ).forEach(
                (x) -> {
                    try {
                        x.invoke(finalInstance);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
        );

        // Get all test methods with tags that we need to run
        List<Method> testMethods = Arrays.stream(classInstance.getMethods())
                .filter((x)-> (x.getAnnotation(OOPTest.class) != null))
                .filter((x) -> {
                    try {
                        return (x.getAnnotation(OOPTest.class)
                                .getClass()
                                .getMethod("tag")
                                .invoke(x.getAnnotation(OOPTest.class))
                                .equals(tag));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return false;
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                        return false;
                    }
                }).collect(Collectors.toList());

        //Invoke all before functions with values containing test_functions
        List<String> testMethodNames = testMethods.stream().
                map((x)-> x.getName()).collect(Collectors.toList());

        Arrays.stream(classInstance.getMethods()).filter(
                (x)-> x.getAnnotation(OOPBefore.class) != null
        ).filter(
                (x) -> {
                    try {
                        String[] beforeNames =
                                (String[]) x.getAnnotation(OOPBefore.class).getClass()
                                .getMethod("value")
                                .invoke(x.getAnnotation(OOPBefore.class));
                        return !intersection(testMethodNames,Arrays.asList(beforeNames)).isEmpty();
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
        ).forEach(
                (x) -> {
                    try {
                        x.invoke(finalInstance);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
        );


        // Invoke all test methods with tags, "" for default
        testMethods.stream().forEach(
                (x) -> {
                    try {
                        x.invoke(finalInstance);
                        OOPResult testResult = new OOPResultImpl(OOPResult.OOPTestResult.SUCCESS);
                        testResults.put(x.getName(),testResult);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        Object thrown = e.getTargetException();
                        OOPExpectedException expectedException = null;
                        List<Field> lRuleField =            // check wheather there exists a rule
                                Arrays.stream(finalInstance.getClass().getFields()).filter(
                                        (rule)-> rule.getAnnotation(OOPExceptionRule.class) != null
                                ).collect(Collectors.toList());
                        if(lRuleField.isEmpty()){           // No Rule - Thrown Exception
                            if(thrown instanceof OOPAssertionFailure){      //Check if assertion failure
                                OOPResult testResult = new OOPResultImpl(
                                        OOPResult.OOPTestResult.FAILURE,
                                        e.getTargetException().getMessage());
                                testResults.put(x.getName(),testResult);
                            } else {                        // Thrown bad Exception
                                OOPResult testResult = new OOPResultImpl(
                                        OOPResult.OOPTestResult.ERROR,
                                        e.getTargetException().getClass().getName());
                                testResults.put(x.getName(),testResult);
                            }

                        }
                        else {          //Exists a rule
                            try {
                                expectedException = (OOPExpectedException) lRuleField.get(0).get(finalInstance);
                                if(thrown instanceof OOPAssertionFailure){      //Check if assertion failure
                                    OOPResult testResult = new OOPResultImpl(
                                            OOPResult.OOPTestResult.FAILURE,
                                            e.getTargetException().getMessage());
                                    testResults.put(x.getName(),testResult);
                                }
                                else if(expectedException.getExpectedException() == null){
                                    // Exists a rule but not expecting exception, yet thrown
                                    OOPResult testResult = new OOPResultImpl(
                                            OOPResult.OOPTestResult.ERROR,
                                            e.getTargetException().getClass().getName());
                                    testResults.put(x.getName(),testResult);
                                } else if(expectedException.assertExpected((Exception) thrown)){
                                    // Expected Exception Caught
                                    OOPResult testResult = new OOPResultImpl(OOPResult.OOPTestResult.SUCCESS);
                                    testResults.put(x.getName(),testResult);
                                }
                                else {                            //Exception Mismatch
                                    OOPResult testResult = new OOPResultImpl(
                                            OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH,
                                            new OOPExceptionMismatchError(expectedException.getExpectedException()
                                                    ,thrown.getClass().asSubclass(Exception.class)).getMessage());
                                    testResults.put(x.getName(),testResult);
                                }
                            } catch (IllegalAccessException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }
                });

        // Invoke all after methods with tags, "" for default
        Arrays.stream(classInstance.getMethods()).filter(
                (x)-> x.getAnnotation(OOPAfter.class) != null
        ).filter(
                (x) -> {
                    try {
                        String[] beforeNames =
                                (String[]) x.getAnnotation(OOPAfter.class).getClass()
                                        .getMethod("value")
                                        .invoke(x.getAnnotation(OOPAfter.class));
                        return !intersection(testMethodNames,Arrays.asList(beforeNames)).isEmpty();
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
        ).forEach(
                (x) -> {
                    try {
                        x.invoke(finalInstance);

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
        );

        return new OOPTestSummary(testResults);
    }
}
