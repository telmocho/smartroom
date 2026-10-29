// Agent bob in project smartroom

/* Initial beliefs and rules */


// preference(20). Ya no es necesario porque se define en startroom.jcm


/* Initial goals */

!start.

/* Plans */

// Cualquier cambio en el artefacto se ve reflejado en las creencias del agente.
//
//-----------------Con VotingMachine------------------
//
/*+status("close") <- // Este plan es una reacción al ver status de vm a close
    .print("La votación se ha cerrado"). 
+status("open") <-
    .print("La votación se ha abierto").
// La anterior manera de hacerlo    
*/

+vm::status("close") <- // Este plan es una reacción al ver status de vm a close
    .print("La votación se ha cerrado").

// ACLARACIÓN: Lo que empieza por '?' es una consulta de mi estado mental
//             Lo que no, es una operación directamente que hace cosas.

+vm::status("open") <-
    // Mira en tus creencias y dime tu preferencia
    ?preference(P);
    // Como hago focus en vm, puedo consultar mi estado mental directamente para ver las options
    ?vm::options(Options);
    // Decidir voto reciclando el algoritmo que ya implementamos antes
    ?my_vote(Options, P, Papeleta); // Variables en Mayúscula para evitar errores.
    // Enviar voto
    vm::vote(Papeleta);
    .print("He votado: ", Papeleta).

//--------------------------------------------------------------------

// Esta versión (ya no se usa) no utiliza VotingMachine
// Cómo reaccionar a la adición de esta creencia
+open_voting(ConvId, Options, Timeout) <-
    ?preference(P); //Esto se resuelve de 2 maneras.
    ?my_vote(Options, P, V);
    .send(rc, tell, vote(ConvId, V)).

// H: cabeza, T: cola de la lista. P: preferencia, V: voto
my_vote([H|_], P, H) :- P <= H.
my_vote([H|T], P, V) :- my_vote(T, P, V). // :- significa: es un cálculo, la llamada de una función. NO un objetivo
my_vote([H], _, H).

+!start : true
    <- .print("hello world.");
       .date(Y,M,D); .time(H,Min,Sec,MilSec); // get current date & time
       +started(Y,M,D,H,Min,Sec);             // add a new belief
       // +temperature(25).                      // add a new belief
        ?preference(P);                          // query a belief. A veces, es necesario hacer consultas mentales del propio agente.
        .print("La preferencia de temperatura es ", P).

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moise/asl/org-obedient.asl") }
