package ontology;

import java.io.*;
import cartago.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/*  EJEMPLOS DE TEST

+!start <-
   o::loadOntology("ontologia/ontologia-uclm.owx");
   !individualsQueryTest;
   !objectProperyQueryTest;
   !dataPropertyQueryTest;

+!individualsQueryTest <-
   getIndividualsOfClass(
      "http://www.semanticweb.org/victor/ontologies/2025/7/ontologia-uclm#Alumno",
      Estudiantes
   );
   .print(" -- Estudiantes ---");
   !print_list(Estudiantes).

+!print_list([]).

+!print_list([H|T]) <-
    .print(H);
    !print_list(T).


+!objectProperyQueryTest <-
    o::getObjectPropertyValues(
        "http://www.semanticweb.org/victor/ontologies/2025/7/ontologia-uclm#alu001",
        "http://www.semanticweb.org/victor/ontologies/2025/7/ontologia-uclm#matriculado",
        Matricula
    );
    .print("Matrícula: ", Matricula).

+!dataPropertyQueryTest <-
    o::getDataPropertyValues(
        "http://www.semanticweb.org/victor/ontologies/2025/7/ontologia-uclm#alu001",
        "http://www.semanticweb.org/victor/ontologies/2025/7/ontologia-uclm#nombrePersona",
        Values
    );
    .print("Nombre: ", Values).

*/


// Fuente: https://github.com/owlcs/owlapi/wiki/Documentation
public class OntologyArtifact extends Artifact {
    IRI iri;
    OWLOntologyManager manager;
    OWLOntology onto;
    OWLDataFactory df;
    PrefixManager pm;

    @OPERATION
    void createClass(String className) {
        try {
            OWLClass clase = df.getOWLClass(IRI.create(iri + className));
            manager.addAxiom(onto, df.getOWLDeclarationAxiom(clase));
        } catch (Exception e) {
            failed("[Ontology] Error creando la clase: " + e.getMessage());
        }
    }

    @OPERATION
    void createOntology(String ontologyName) {
        try {
            manager = OWLManager.createOWLOntologyManager();
            df = manager.getOWLDataFactory();
            iri = IRI.create("http://jacamo.org/onto#");
            pm = new DefaultPrefixManager(null, null, iri.toString());
            onto = manager.createOntology(iri);
        } catch (Exception e) {
            failed("[Ontology] Error creando la ontología: " + e.getMessage());
        }
    }

    /**
     * Guarda una ontología
     * @param fileName ruta relativa del fichero donde se va a guardar la ontología.
     */
    @OPERATION
    void saveOntology(String fileName) {
        try {
            File newFile = new File(fileName);
            if ( newFile.createNewFile() )
                manager.saveOntology(onto, IRI.create(newFile.toURI()));
                System.out.println("Saved ontology: " + onto);
        } catch(IOException e){
            e.printStackTrace();
        }catch (Exception e) {
            failed("[Ontology] Error guardando la ontología: " + e.getMessage());
        }
    }

    /**
     * Cargar una ontología.
     * @param ontologyName nombre de la ontología.
     */
    @OPERATION 
    void loadOntology(String ontologyName) {
        manager = OWLManager.createOWLOntologyManager();
        
        try {
           onto = manager.loadOntologyFromOntologyDocument(IRI.create(new File(ontologyName).toURI()));
           df = manager.getOWLDataFactory();
           iri = manager.getOntologyDocumentIRI(onto);
           pm = new DefaultPrefixManager(null, null, iri.toString());
           // Para indicar que hay una ontología cargada.
           defineObsProperty("ontologyLoaded", true);

           System.out.println("Loaded ontology: " + onto);
        } catch (Exception e) {
            e.printStackTrace();
            failed("[Ontology] Error guardando la ontología: " + e.getMessage());
        }

        // Imprimir las clases que tiene la ontología
         if (onto!=null) {
            for (OWLClass cls : onto.getClassesInSignature()) {
                System.out.println(cls.toString());

                System.out.println("Properties of " + cls);
                OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
                OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(onto);
                for (OWLObjectPropertyExpression prop : onto.getObjectPropertiesInSignature()) {
                    boolean sat = OntologyArtifact.hasProperty(manager, reasoner, cls, prop);
                    if (sat) {
                        System.out.println("Instances of " + cls
                                + " necessarily have the property " + prop);
                    }
                }
            }
            
            // Imprimir las data properties que tiene cada individuo.
            for (OWLNamedIndividual ind : onto.getIndividualsInSignature()) {
                    System.out.println(ind.getIRI());
                    // Imprimir las data properties que tiene cada individuo.
                    Set<OWLDataPropertyAssertionAxiom> properties = onto.getDataPropertyAssertionAxioms(ind);
                    for (OWLDataPropertyAssertionAxiom objData : properties) {
                        System.out.println(objData.toString());
                    }
                    // Imprimir las object properties que tiene cada individuo.
                    Set<OWLObjectPropertyAssertionAxiom> properties2 = onto.getObjectPropertyAssertionAxioms(ind);
                    for (OWLObjectPropertyAssertionAxiom objData : properties2) {
                        System.out.println(objData.toString());
                    }
            }
        } 
    }

    /**
     * Indica si una propiedad es obligatoria o no para una clase.
     * Fuente: https://github.com/phillord/owl-api/blob/master/contract/src/test/java/org/coode/owlapi/examples/Examples.java
     * @param man
     * @param reasoner
     * @param cls
     * @param prop
     * @return
     */
    private static boolean hasProperty(OWLOntologyManager man, OWLReasoner reasoner, OWLClass cls, OWLObjectPropertyExpression prop) {

        OWLDataFactory dataFactory = man.getOWLDataFactory();
        OWLClassExpression restriction = dataFactory.getOWLObjectSomeValuesFrom(prop,
                dataFactory.getOWLThing());
        OWLClassExpression complement = dataFactory.getOWLObjectComplementOf(restriction);
        OWLClassExpression intersection = dataFactory.getOWLObjectIntersectionOf(cls,
                complement);
        return !reasoner.isSatisfiable(intersection);
    }

    @OPERATION
    public void getIndividualsOfClass(String classIRI, OpFeedbackParam<String[]> individuals) {

        try {

            OWLClass owlClass = df.getOWLClass(IRI.create(classIRI));

            List<String> result = onto
                    .classAssertionAxioms(owlClass)
                    .map(ax -> ax.getIndividual())
                    .filter(ind -> !ind.isAnonymous())
                    .map(ind -> ind.asOWLNamedIndividual()
                                .getIRI()
                                .toString())
                    .toList();

            individuals.set(result.toArray(new String[0]));

        } catch (Exception e) {
            failed("Error retrieving individuals: " + e.getMessage());
        }
    }

    /**
     * Obtiene las instancias de object property asociadas a un individuo.
     * @param individualIRI Individuo (ej. "http://www.semanticweb.org/victor/ontologies/2025/7/ontologia-uclm#alu001" )
     * @param propertyIRI Object property (ej. "http://www.semanticweb.org/victor/ontologies/2025/7/ontologia-uclm#matriculado" )
     * @param result Lista de instancias de la object property indicada para este individuo.
     */
    @OPERATION
    public void getObjectPropertyValues(String individualIRI, String propertyIRI,
            OpFeedbackParam<String[]> result) {

        try {

            OWLNamedIndividual individual = df.getOWLNamedIndividual(IRI.create(individualIRI));
            OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(propertyIRI));

            List<String> values = new ArrayList<>();

            onto.objectPropertyAssertionAxioms(individual)
                    .filter(ax -> ax.getProperty().equals(property))
                    .forEach(ax -> {
                        OWLIndividual obj = ax.getObject();

                        if (!obj.isAnonymous()) {
                            values.add(obj.asOWLNamedIndividual().getIRI().toString());
                        }
                    });

            result.set(values.toArray(new String[0]));

        } catch (Exception e) {
            failed("[Ontology]Object Property Query error: " + e.getMessage());
        }
    }

    /**
     * Obtiene la lista de valores de una data property (habitualmente es único, pero podría
     * no serlo, por eso devuelve una lista).
     * @param individualIRI Individuo (ej. "http://www.semanticweb.org/victor/ontologies/2025/7/ontologia-uclm#alu001" )
     * @param propertyIRI data property (ej. "http://www.semanticweb.org/victor/ontologies/2025/7/ontologia-uclm#nombrePersona" )
     * @param values
     */
    @OPERATION
    public void getDataPropertyValues(String individualIRI, String propertyIRI,
            OpFeedbackParam<String[]> values) {

        try {

            OWLNamedIndividual individual =
                    df.getOWLNamedIndividual(IRI.create(individualIRI));

            OWLDataProperty property =
                    df.getOWLDataProperty(IRI.create(propertyIRI));

            List<String> result = onto
                    .dataPropertyAssertionAxioms(individual)
                    .filter(ax -> ax.getProperty().equals(property))
                    .map(ax -> ax.getObject().getLiteral())
                    .toList();

            values.set(result.toArray(new String[0]));

        } catch (Exception e) {
            failed("[Ontology] Error querying data property: " + e.getMessage());
        }
    }
}