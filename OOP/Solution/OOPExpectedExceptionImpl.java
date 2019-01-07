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
        if(mExpected == null || e == null){
            return false;
        }
        return (mExpected.isInstance(e)
                && (mExpectedMessages.size() == 0 | mExpectedMessages.stream().filter(
                (x) -> {
                    if(e.getMessage() == null){
                        return false;
                    } else {
                        return e.getMessage().indexOf(x) != -1;
                    }
                }
        ).count() == mExpectedMessages.size()

        )); //Add contains message as substring
    }
}
