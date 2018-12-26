package OOP.Solution;

import OOP.Provided.OOPResult;

public class OOPResultImpl implements OOPResult {
    private String mMessage;
    private OOPTestResult mResult;

    public OOPResultImpl(OOPTestResult result){
        mResult = result;
    }
    public OOPResultImpl(OOPTestResult result, String msg){
        mMessage = msg;
        mResult = result;
    }

    @Override
    public OOPTestResult getResultType() {
        return mResult;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }

    @Override
    public boolean equals(Object ob){
        if(ob.getClass() == this.getClass()){
            OOPResultImpl lObject = (OOPResultImpl) ob;
            if(lObject.getMessage() == null ){
                if(this.getMessage() == null){
                    return true;
                }
                return false;
            }
            if((lObject.getMessage().equals(this.getMessage()))
                    && (lObject.getResultType().equals(this.getResultType()))){
                return true;
            }
        }
        return false;
    }
}
