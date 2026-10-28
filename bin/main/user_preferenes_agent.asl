// Agent bob in project smartroom

/* Initial beliefs and rules */

temperature(22). // initial temperature
preference(20).
temperature(aula1_4, 20). // initial temperature in aula1_4
happy(calamardo). // calamardo is happy
happy(bob).      // bob is happy


/* Initial goals */

!temperature(20). 
!start.

/* Plans */

/*+temperature(30) <- !temperature(20).*/
+!temperature(20) <- startCooling.
+!happy(bob) <- kiss(bob).

/*Ways of doing the same thing to achieve the goals*/
/*+temperature(25) <- startCooling;
    .print("Temperatura 25");
    startCooling.
*/
/*+temperature(20) <-
    .print("Temperatura 20");
    stopAirConditioner.*/

+!printHolaMundo(N) <- .wait(1000); .print("Hola mundo, soy ", N).

+?preference(X) <- .print("Test preferencias"). 

-!printHolaMundo(N) <- .print("Ya no quiero saludar"). 
// Esta parte nunca se ejecuta porque no se puede
// cancelar un goal que ya se ha alcanzado (printHolaMundo se ejecutó antes),
// pero si se puede cancelar un goal que no se ha alcanzado.

+temperature(T) : preference(P) & math.abs(T-P)>2 <-
    !temperature(P);
    .print("La temperatura es ", T).

+temperature(T) : preference(T) <-
    stopAirConditioner.

+happy (X)[source(S)] : S = bob <-
    .print("Que dice ", X, " que es feliz.").

-happy(X) : X = bob <-
    .print("Pobrecito ", X).

+!kqml_received(Agent, inform, C, _) <- // Esa recepción es del último inform de sample_agent.asl
    +C; // C es happy(victor) y se añade a las creencias del agente. El agente ahora cree que Victor es feliz.
    .print("Llego: ", C).

+happy(victor) <-
    .print("Victor es feliz.").

// Cuando a un agente le llega un tell, le modifica el estado mental y sus creencias.
// Pero inform no lo hace. Por eso, no funciona a solas desde el otro agente.


+!start : true
    <- .print("hello world.");
       .date(Y,M,D); .time(H,Min,Sec,MilSec); // get current date & time
       +started(Y,M,D,H,Min,Sec);             // add a new belief
       // +temperature(25).                      // add a new belief
        ?preference(P);                          // query a belief. A veces, es necesario hacer consultas mentales del propio agente.
        print("La preferencia de temperatura es ", P).

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moise/asl/org-obedient.asl") }
