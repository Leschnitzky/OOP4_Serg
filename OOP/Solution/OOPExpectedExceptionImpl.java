package OOP.Solution;

import OOP.Provided.OOPExpectedException;

import java.util.ArrayList;

public class OOPExpectedExceptionImpl implements OOPExpectedException {

    private Class<? extends Exception> mExpected;
    private ArrayList<String> mExpectedMessages;

    private OOPExpectedExceptionImpl(){
        mExpected = null;
        mExpectedMessages = new ArrayList<>();
    }

    public static OOPExpectedException none(){
        return new OOPExpectedExceptionImpl();
    }

    @Override
    public Class<? extends Exception> getExpectedException() {
        if( mExpected == null){
            return null;
        }
        return mExpected;
    }

    @Override
    public OOPExpectedException expect(Class<? extends Exception> expected) {
        this.mExpected = expected;
        return this;
    }

    @Override
    public OOPExpectedException expectMessage(String msg) {
        if(!mExpectedMessages.contains(msg)){
            mExpectedMessages.add(msg);
        }
        return this;
    }

    @Override
    public boolean assertExpected(Exception e) {
        if(mExpected == null){
            return false;
        }
        return (mExpected.isInstance(e) && mExpectedMessages.stream().filter(
                (x) -> {return (e.getMessage().indexOf(x) != 0);}
        ));
    }
}
