/*************************************************************************
 *  Dependencies: None
 *
 *  A spring object for physically based modeling applications.
 *  
 *  @author Alexandros Charidis, MIT
 *************************************************************************/

public class Spring {

	private int from_, to_;
    private float ks_, kd_;
    private float restl_;
    private float F_;
    private float L_;
    
    // default empty constructor
    public Spring() { }
    public Spring(int from, int to) { 
        from_ = from;
        to_   = to;
    }
    
    public void setSpringConstant(float ks)  {  ks_ = ks; }
    public void setDampingConstant(float kd) {  kd_ = kd; }
    public void setRestLength(float restl) {  restl_ = restl;  }
    public void setForce(float F) {  F_ = F;  }
    public void setLength(float L) {  L_ = L;  }
    
    public int from() {  return from_;  }
    public int to()   {  return to_;    }
    
    public float restLength() { return restl_;  }
    public float kSpring()    { return ks_; }
    public float kDamp()      { return kd_; }
    public float F()          { return F_;  }
    public float L()          { return L_;  }
	
	public static void main(String[] args) { }

}
