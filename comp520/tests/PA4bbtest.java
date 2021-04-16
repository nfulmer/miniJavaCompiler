class PA4Test
{
    public static void main(String[] args)
    {        
        /* 1: simple literal */
        int x = 1;
        //System.out.println(x);  
        /*int y = 5;
        System.out.println(y);
        int z = 42;
        System.out.println(z);
        
        System.out.println(z);
        System.out.println(x);
        System.out.println(y);*/
        
        /*x = 2 * x + x - 1;
        //System.out.println(x); 
        
        int y = -1;
        //System.out.println(y);
        
        y = -x + y;
        //System.out.println(y);
        
        //System.out.println(3);
        
        x = 0;
        
        if (x != -1)
        	System.out.println(4);
        else
            System.out.println(-1);
        
        int i = 0;
        while (i < 5) {
            i = i + 1;
            x = i*2;
        }
        System.out.println(i);
        System.out.println(x);
        
        boolean bb = x >= 100000 || true;
        
        if (bb) 
        	System.out.println(666);
        else
        	System.out.println(42);
        */
        /* 6: object creation */
        A a = new A();
        //a = null;
        if (a != null)
        	System.out.println(6);
        
        /* 7: field reference */
        //x = 7 + a.n;
        //System.out.println(x);
        
        //a.n = 72;
        //x = a.n - x;
        //x = a.n - 4;
        //System.out.println(x);
        
        /* 8: qualified reference and update */
        a.b = new B();
        a.b.n = 8;
        if (a.b != null)
        	System.out.println(6);
        System.out.println(a.b.n);
        System.out.println(a.n);
    }
    
    
}

class A
{
    int n;
    B b;
}

class B
{
    int n;
}
