package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExceptionMismatchError;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;
import OOP.Tests.ExampleTest;
import OOP.Tests.IntegrationTests.PartThreeIntegrationTests;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OOPUnitCore {

    private static boolean tagless = false;

    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<>();

        for (T t : list1) {
            if (list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

    public static int getClassHeierchy(Method x){
        int rank = 0;
        Class classOfMethod = x.getDeclaringClass();
        while(classOfMethod != Object.class){
            rank++;
            classOfMethod = classOfMethod.getSuperclass();
        }
        return rank;
    }

    public static HashMap<Field,Object> backupObject(Object finalInstance){
        HashMap<Field,Object> cloneValues = new HashMap<>();
        Arrays.stream(finalInstance.getClass().getDeclaredFields()).forEach(
                (field) -> {
                    field.setAccessible(true);
                    if(field.getType().isPrimitive()){  // field is primitive, get value
                        try {
                            cloneValues.put(field,field.get(finalInstance));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    else try {
                        if(field.get(finalInstance) instanceof Cloneable){      //Check if the field is clonable
                            try {
                                Method cloneMethod = field.getType().getMethod("clone");
                                cloneMethod.setAccessible(true);
                                cloneValues.put(field,cloneMethod.invoke(field.get(finalInstance)));
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                //Shouldnt get here - has method clone/can't throw exception (not runtime)/ has access
                                e.printStackTrace();
                            }
                            //Check if has a copyConstructor
                        } else if(Arrays.stream(field.getType().getDeclaredConstructors()).filter(
                                (constructor) -> Stream.of(constructor.getParameterTypes()).filter(
                                        (arg) -> {return arg == field.getType();}
                                ).count() > 0
                        ).count() > 0){
                            try {
                                Constructor copyConstructor = field.getType().getConstructor(field.getType());
                                cloneValues.put(field,copyConstructor.newInstance(field.get(finalInstance)));
                            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        } else {  //clone by value
                            try {
                                cloneValues.put(field,field.get(finalInstance));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
        );
        return cloneValues;
    }


    public static OOPTestSummary runClass(Class<?> exampleClassClass) {
        if(exampleClassClass == null){
            throw new IllegalArgumentException();
        }
        tagless = true;
        return runClass(exampleClassClass,"");
    }

    public static void assertEquals(Object first, Object second) {
        if((first == null) && (second == null)){
            return;
        }
        if(first == null && second != null){
            throw new OOPAssertionFailure();
        }
        if(!first.equals(second)){
            throw new OOPAssertionFailure();
        }
    }

    public static void fail() {
        throw new OOPAssertionFailure();
    }

    public static OOPTestSummary runClass(Class<?> classInstance, String tag) {
        if(tag == null || classInstance == null){
            throw new IllegalArgumentException();
        }

        if (classInstance.getAnnotation(OOPTestClass.class) == null){
            throw new IllegalArgumentException();
        }

        Object instance = null; // Creates a new instance
        HashMap<String,OOPResult> testResults = new HashMap<>();

        //Backup Object
        final HashMap<Field, Object>[] cloneValues = new HashMap[1];
        ArrayList<String> testErrors = new ArrayList<>();
        try {
            Constructor constructor = classInstance.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // Invoke all setup methods

        Object finalInstance = instance;


        Arrays.stream(classInstance.getMethods()).filter((x)-> (x.getAnnotation(OOPSetup.class) != null)
        ).sorted(
                //Sort from father to son
                Comparator.comparingInt(x -> (getClassHeierchy(x))))
                .forEach(
                (setup) -> {
                    try {
                        setup.invoke(finalInstance);
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
                    try {if(tagless){
                        return true;
                    }else {
                        return (x.getAnnotation(OOPTest.class)
                                .getClass()
                                .getMethod("tag")
                                .invoke(x.getAnnotation(OOPTest.class))
                                .equals(tag));
                    }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return false;
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                        return false;
                    }
                }).collect(Collectors.toList());

        if(finalInstance.getClass().getAnnotation(OOPTestClass.class).value() == OOPTestClass.OOPTestClassType.ORDERED){
            testMethods = testMethods.stream()          //Sort by order
                    .sorted(
                            (x,y) -> {
                                int x_order = x.getAnnotation(OOPTest.class).order();
                                int y_order = y.getAnnotation(OOPTest.class).order();
                                if(x.getDeclaringClass() != finalInstance.getClass()){
                                    x_order = 0;        // change to 0 if the declaring class is father
                                }
                                if(y.getDeclaringClass() != finalInstance.getClass()){
                                    y_order = 0;        // change to 0 if the declaring class is father
                                }
                                return x_order - y_order;
                            }
                    )
                    .collect(Collectors.toList());
        }



        // Backup the object before the before methods
        cloneValues[0] = backupObject(finalInstance);

        //Collect all before functions with values containing test_functions
        List<String> testMethodNames = testMethods.stream().
                map((x)-> x.getName()).collect(Collectors.toList());

        HashMap<Field, Object> finalCloneValues = cloneValues[0];

        //Init all testMethods in beforeMap, afterMap
        HashMap<String, ArrayList<Method>> beforeMethods = new HashMap<>();
        HashMap<String, ArrayList<Method>> afterMethods = new HashMap<>();

        for(String testMethodName : testMethodNames){
            beforeMethods.put(testMethodName,new ArrayList<>());
            afterMethods.put(testMethodName,new ArrayList<>());
        }

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
        )       //Invoke all father befores first
                .sorted(
                        // sort by order of invokation (Father before son)
                        Comparator.comparingInt(OOPUnitCore::getClassHeierchy)
                ).forEach(
                (beforeMethod) -> {
                    for( String testName: beforeMethod.getAnnotation(OOPBefore.class).value()){
                        if(beforeMethods.keySet().contains(testName)){
                            beforeMethods.get(testName).add(beforeMethod);
                        }
                    }
                }

        );

        //Collect all after methods
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
        ).sorted(
                // sort by order of invocation (son before father)
                (x,y) -> getClassHeierchy(y) - getClassHeierchy(x)
        ).filter(

                // remove all after tests where all tests in them got errors
                (method) ->
                        Arrays.stream(method.getAnnotation(OOPAfter.class).value()).filter(
                                (test) ->
                                        !testErrors.contains(test)
                        ).count() > 0

        )
                .forEach(
                        (afterMethod) -> {
                            for( String testName: afterMethod.getAnnotation(OOPAfter.class).value()){
                                if(afterMethods.keySet().contains(testName)){
                                    afterMethods.get(testName).add(afterMethod);
                                }
                            }
                        }
                );


        cloneValues[0] = backupObject(finalInstance);

        List<Field> lRuleField =            // check wheather there exists a rule
                Arrays.stream(finalInstance.getClass().getDeclaredFields()).filter(
                        (rule)-> rule.getAnnotation(OOPExceptionRule.class) != null
                ).collect(Collectors.toList());

        // Invoke all test methods with tags that have no errors in OOPBefore, "" for default
        testMethods.stream().filter(
                (x) -> !testErrors.contains(x.getName())
        ).sorted((x,y)-> {
            if(x.getAnnotation(OOPTest.class).order() == y.getAnnotation(OOPTest.class).order()){
                return getClassHeierchy(x) - getClassHeierchy(y);
            } else{
                return x.getAnnotation(OOPTest.class).order() - y.getAnnotation(OOPTest.class).order();
            }
        })
                .forEach(
                        //Invoke Before Methods
                        (test) -> {
                            //Backup before OOPBEFORE invocation
                            cloneValues[0] = backupObject(finalInstance);
                            beforeMethods.get(test.getName()).stream().forEach(
                                    (beforeMethod) -> {
                                        try {
                                            beforeMethod.invoke(finalInstance);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
//                                        OOPBEFORE failed - The current method gets a test error
                                            testResults.put(test.getName(),
                                                    new OOPResultImpl(OOPResult.OOPTestResult.ERROR,
                                                            e.getTargetException().toString()));


                                            //Add tests to error list, to ignore them later
                                            testErrors.add(test.getName());

                                            //Restore Object

                                            finalCloneValues.keySet().forEach(
                                                    (field) ->
                                                    {
                                                        field.setAccessible(true);
                                                        try {
                                                            field.set(finalInstance, finalCloneValues.get(field));
                                                        } catch (IllegalAccessException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                    }
                                            );

                                        }
                                    }
                            );

                            //Invoke actual test
                            try {
                                //clear ExpectedException
                                List<Field> expectedField = Arrays.stream(finalInstance.getClass().getDeclaredFields()).filter(
                                        (rule) -> rule.getAnnotation(OOPExceptionRule.class) != null
                                ).collect(Collectors.toList());

                                if (expectedField.size() == 0) {
                                } else {
                                    expectedField.get(0).setAccessible(true);
                                    try {
                                        expectedField.get(0).set(finalInstance, OOPExpectedException.none());
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if(testErrors.contains(test.getName())){ }
                                else {
                                    test.invoke(finalInstance);
                                    if(!lRuleField.isEmpty()){
                                        lRuleField.get(0).setAccessible(true);
                                        OOPExpectedException expected = (OOPExpectedException)
                                                lRuleField.get(0).get(finalInstance);
                                        if(expected.getExpectedException() != null) {
                                            OOPResult testResult = new OOPResultImpl(OOPResult.OOPTestResult.ERROR,
                                                    expected.getExpectedException().getName());
                                            testResults.putIfAbsent(test.getName(), testResult);
                                        } else{
                                            OOPResult testResult = new OOPResultImpl(OOPResult.OOPTestResult.SUCCESS);
                                            testResults.putIfAbsent(test.getName(), testResult);
                                        }
                                    }
                                    else {
                                        OOPResult testResult = new OOPResultImpl(OOPResult.OOPTestResult.SUCCESS);
                                        testResults.putIfAbsent(test.getName(), testResult);
                                    }
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                Object thrown = e.getTargetException();
                                OOPExpectedException expectedException = null;
                                if(lRuleField.isEmpty()){           // No Rule - Thrown Exception
                                    if(thrown instanceof OOPAssertionFailure){      //Check if assertion failure
                                        OOPResult testResult = new OOPResultImpl(
                                                OOPResult.OOPTestResult.FAILURE,
                                                e.getTargetException().getMessage());
                                        testResults.putIfAbsent(test.getName(),testResult);
                                    } else {                        // Thrown bad Exception
                                        OOPResult testResult = new OOPResultImpl(
                                                OOPResult.OOPTestResult.ERROR,
                                                e.getTargetException().getClass().getName());
                                        testResults.putIfAbsent(test.getName(),testResult);
                                    }

                                }
                                else {          //Exists a rule
                                    try {
                                        lRuleField.get(0).setAccessible(true); // get field as accessible
                                        expectedException = (OOPExpectedException) lRuleField.get(0).get(finalInstance);
                                        if(thrown instanceof OOPAssertionFailure){      //Check if assertion failure
                                            OOPResult testResult = new OOPResultImpl(
                                                    OOPResult.OOPTestResult.FAILURE,
                                                    e.getTargetException().getMessage());
                                            testResults.putIfAbsent(test.getName(),testResult);
                                        }
                                        else if(expectedException.getExpectedException() == null){
                                            // Exists a rule but not expecting exception, yet thrown
                                            OOPResult testResult = new OOPResultImpl(
                                                    OOPResult.OOPTestResult.ERROR,
                                                    e.getTargetException().getClass().getName());
                                            testResults.putIfAbsent(test.getName(),testResult);
                                        } else if(expectedException.assertExpected((Exception) thrown)){
                                            // Expected Exception Caught
                                            OOPResult testResult = new OOPResultImpl(OOPResult.OOPTestResult.SUCCESS);
                                            testResults.putIfAbsent(test.getName(),testResult);
                                        }
                                        else {                            //Exception Mismatch
                                            OOPResult testResult = new OOPResultImpl(
                                                    OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH,
                                                    new OOPExceptionMismatchError(expectedException.getExpectedException()
                                                            ,thrown.getClass().asSubclass(Exception.class)).getMessage());
                                            testResults.putIfAbsent(test.getName(),testResult);
                                        }
                                    } catch (IllegalAccessException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                            }
                            //Reset ExpectedException between tests
                            finally {

                                //Backup before OOPAfter invocation
                                if (testErrors.contains(test.getName())) {
                                }        //If test has error, skip running OOPAfter
                                else {
                                    cloneValues[0] = backupObject(finalInstance);
                                    afterMethods.get(test.getName()).forEach(
                                            (after) -> {
                                                try {
                                                    after.invoke(finalInstance);

                                                } catch (IllegalAccessException e) {
                                                    e.printStackTrace();
                                                } catch (InvocationTargetException e) {

                                                    //Change the current method
                                                    testResults.put(test.getName(),
                                                            new OOPResultImpl(OOPResult.OOPTestResult.ERROR,
                                                                    e.getTargetException().toString())
                                                    );
                                                    //Restore Object
                                                    cloneValues[0].keySet().forEach(
                                                            (field) ->
                                                            {
                                                                field.setAccessible(true);
                                                                try {
                                                                    field.set(finalInstance, cloneValues[0].get(field));
                                                                } catch (IllegalAccessException e1) {
                                                                    e1.printStackTrace();
                                                                }
                                                            }
                                                    );
                                                }
                                            }


                                    );


                                }
                            }
                        });
        tagless = false;
        return new OOPTestSummary(testResults);
    }
}
