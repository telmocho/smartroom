// Agent bob in project smartroom

/* Initial beliefs and rules */
preference(24).


/* Initial goals */

!start.

/* Plans */

+!start : true
    <- .print("hello world.");
       .date(Y,M,D); .time(H,Min,Sec,MilSec); // get current date & time
       +started(Y,M,D,H,Min,Sec);
       .my_name(N);
       // tell
       .send(rc, tell, happy(bob));                     // add a new belief
       // untell
       .send(rc, untell, happy(bob));                   // add a new belief
       // achieve
       .send(rc, achieve, printHolaMundo(N));           // add a new belief
       // unachieve
       .send(rc, unachieve, printHolaMundo(N));        // add a new belief
       // askOne asincrono
       .send(rc, askOne, preference(P));                 // add a new beliefç. Añadir el parámetro hace la llamada síncrona.
       // askOne sincrono
       .send(rc, askOne, happy(X), A);                 // add a new beliefç. Añadir el parámetro hace la llamada síncrona.
       // .wait(500); En principio, wait no haría falta porque askOne es síncrono y bloquea hasta que se recibe la respuesta, pero si se pone un wait, se puede ver el efecto de la llamada asíncrona.
       .print(A);
       // askAll asincrono
       .send(rc, askAll, happy(X), Lista);
       .nth(0 , Lista, Cabeza); // Pas, Lista, Variable destino
       .print("La primera persona feliz es ", Cabeza);
       // tellHow añade un plan a un agente remoto. El plan se añade al final de la lista de planes del agente remoto.
       .send(rc, tellHow, "@pSaludar +!saludar(X) <- .print(\"¡Hola: \", X).");
       .wait(500);       
       .send(rc, achieve, saludar("Paco")); //Esta línea da el goal saludar("Paco")  al agente remoto rc y ejecuta el plan. El plan pSaludar se ejecuta y se imprime "¡Hola: Paco".
         // untellHow borra el plan pSaludar y por tanto no se puede ejecutar el goal saludar("Paco").
        .send(rc, untellHow, "@pSaludar");
        .wait(500);
        .send(rc, achieve, saludar("Paco")); // Esta línea dara error porque untellHow ha borrado el plan pSaludar y por tanto no se puede ejecutar el goal saludar("Paco").
        // AskHow
        .send(rc, askHow, "+?preference(X)");
        .wait(500);
        ?preference(E3);
        .print("Mi preferencia es", E3);
        .send(rc, signal, happy(bob));
        .send(rc, inform, happy(victor)). // eso será procesado por el plan +!kqml_received(Agent, inform, C, _) del agente remoto rc. C será happy(victor) y se añadirá a las creencias del agente remoto rc. El agente remoto rc ahora cree que Victor es feliz.


{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moise/asl/org-obedient.asl") }
