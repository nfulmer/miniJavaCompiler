  0         LOADL        0
  1         PUSH         1
  2         LOADL        1
  3         STORE        0[LB]
  4         PUSH         1
  5         LOADL        -1
  6         LOADL        2
  7         CALL         newobj  
  8         STORE        1[LB]
  9         LOADL        1
 10         LOADI  
 11         LOADL        0
 12         CALL         ne      
 13         JUMPIF (0)   L10
 14         LOADL        6
 15         CALL         putintnl
 16         JUMP         L10
 17  L10:   LOAD         1[LB]
 18         LOADL        1
 19         LOADL        1
 20         LOADL        -1
 21         LOADL        1
 22         CALL         newobj  
 23         CALL         fieldupd
 24         LOAD         1[LB]
 25         LOADL        1
 26         CALL         fieldref
 27         LOADL        0
 28         LOADL        8
 29         CALL         fieldupd
 30         LOAD         1[LB]
 31         LOADL        1
 32         CALL         fieldref
 33         LOADL        0
 34         CALL         ne      
 35         JUMPIF (0)   L11
 36         LOADL        6
 37         CALL         putintnl
 38         JUMP         L11
 39  L11:   LOAD         1[LB]
 40         LOADL        1
 41         CALL         fieldref
 42         CALL         putintnl
 43         LOAD         1[LB]
 44         LOADL        0
 45         CALL         fieldref
 46         CALL         putintnl
 47         HALT   (0)   
