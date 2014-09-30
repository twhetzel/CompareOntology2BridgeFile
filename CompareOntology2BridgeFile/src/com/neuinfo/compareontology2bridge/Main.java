package com.neuinfo.compareontology2bridge;

import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	/**
	 * Compare Ontology file to be removed from the import closure with
	 * Bridge ontology file to confirm all Classes in the Ontology file are
	 * included in the Bridge file
	 * 
	 * NOTE: Some GO files on the web use updated NIF IRIs (http://uri.neuinfo.org/nif/nifstd/someNumber) 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Trying to load ontology...");
			OWLOntology ontology = loadOntologyFile();
			//System.out.println(ontology);
			Set<OWLClass> allClasses = getAllClasses(ontology);
			//System.out.println("ALLCLASSES: "+allClasses);

			OWLOntology bridgeOntology = loadBridgeFile();

			// Check that all terms in Ontology file have an Equivalent class in the Bridge ontology file
			searchForClassInBridgeOntology(allClasses, bridgeOntology);

			System.out.println("** Program Complete **");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}	
	}


	/** 
	 * Load Ontology file (the one to be removed from the import)
	 * @return 
	 * 
	 * @throws OWLOntologyCreationException 
	 **/
	@Test
	public static OWLOntology loadOntologyFile() throws OWLOntologyCreationException {
		//TODO Pass ontology location as a command-line argument 
		// Load ontology from web
		/*IRI ONTOLOGY = IRI.create("http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Subcellular.owl"); // IRIs not updated, use with NIF-GO-CC-Bridge.owl only!
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(ONTOLOGY); 
*/
		
		// Load ontology from local file
		File file = new File("/Users/whetzel/Documents/workspace/OntologyFiles/NIF-Subcellular-ORIG.owl");  //updated IRIs, use with go-nifstd-bridge.owl only
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

		System.out.println("Loaded ontology: " + ontology);
		return ontology;
	}

	
	/**
	 * Get all Classes in ontology file
	 * @param manager
	 * @param ontology
	 * @return 
	 */
	private static Set<OWLClass> getAllClasses(OWLOntology ontology) {
		Set<OWLClass> allClasses = ontology.getClassesInSignature();
		int size = allClasses.size();
		
		System.out.println("Total number of classes: "+size);
		return allClasses;
	}
	

	/**
	 * Load ontology Bridge file
	 * @return 
	 * @throws OWLOntologyCreationException 
	 */
	private static OWLOntology loadBridgeFile() throws OWLOntologyCreationException {
		//TODO Pass ontology location as a command-line argument 
		// Load ontology from web
		//IRI NIF_GO_CC_BRIDGE_ONTOLOGY = IRI.create("http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-GO-CC-Bridge.owl"); //Use with NIF-Subcellular.owl from web so IRI formats match 
		IRI GO_NIFSTD_BRIDGE_ONTOLOGY = IRI.create("http://geneontology.org/ontology/extensions/go-nifstd-bridge.owl"); // Compare to Local NIF-Subcellular.owl file with updated IRIs
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(GO_NIFSTD_BRIDGE_ONTOLOGY); 
		 
		System.out.println("Loaded ontology: " + ontology);
		return ontology;
	}


	/**
	 * Check for Ontology Class in Bridge ontology file
	 * @param ontology
	 * @param bridgeOntology
	 * @throws OWLOntologyCreationException 
	 */
	private static void searchForClassInBridgeOntology(Set<OWLClass> allClasses,
			OWLOntology bridgeOntology) throws OWLOntologyCreationException {
		// Iterate through Ontology
		int size = allClasses.size();
		System.out.println("Total number of classes: "+size);

		ArrayList<String> noEquivClass = new ArrayList<String>();

		for (OWLClass owlClass : allClasses) {
			System.out.println("NIFSTD OWLCLASS: "+owlClass); 

			bridgeOntology.getEquivalentClassesAxioms(owlClass);
			System.out.println("BRIDGE OWLCLASS: "+owlClass);
			System.out.println ("EQUIVCLASSES: "+bridgeOntology.getEquivalentClassesAxioms(owlClass)+"\n");

			if (bridgeOntology.getEquivalentClassesAxioms(owlClass).isEmpty()) {
				noEquivClass.add(owlClass.toString());
			}
			else {
				System.out.print("Looks good - ");
				System.out.println ("Found an Equivalent Class: "+bridgeOntology.getEquivalentClassesAxioms(owlClass)+"\n");
			}
		}	
		// Write out results to file 
		writeFile(noEquivClass);
	}


	/**
	 * Write content to output file
	 * @param noEquivClass 
	 * @throws OWLOntologyCreationException 
	 */
	public static void writeFile(ArrayList<String> noEquivClass) throws OWLOntologyCreationException {
		try {
			//TODO Pass output file name as parameter  
			File file = new File("./noEquivClassList_go-nifstd_09292014-B.txt");
			int size = noEquivClass.size();

			// If file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Total number of Classes WITHOUT an Equivalent Axiom: "+size+"\n");
			for (String term : noEquivClass) {
				bw.write(term+"\t");
				//TODO Get Class label and parent Class label
				// First, get rdfs:label of Class
				ArrayList<String> termInfo = getClassLabelAndParent(term);
				for (String s : termInfo) {
					bw.write("\t"+s);
				}
				bw.write("\n");
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static ArrayList<String> getClassLabelAndParent(String term) throws OWLOntologyCreationException {
		//System.out.println("DEBUG Class IRI: "+term);
		
		// Open ontology again to get term details
		File file = new File("/Users/whetzel/Documents/workspace/OntologyFiles/NIF-Subcellular-ORIG.owl");  //updated IRIs, use with go-nifstd-bridge.owl only
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
		
		OWLDataFactory df = manager.getOWLDataFactory();
		OWLAnnotationProperty label = df
                .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		//System.out.println("LabelProp: "+label);
		
		// Iterate through all classes and for those that match list of terms without an equivalent class, get term details
		ArrayList<String> termInfo = new ArrayList();
		for (OWLClass cls : ontology.getClassesInSignature()) {
			if (cls.toString().equals( term )) {
				
				// Get term label
				for (OWLAnnotation annotation : cls.getAnnotations(ontology, label)) {
					if (annotation.getValue() instanceof OWLLiteral) {
						OWLLiteral val = (OWLLiteral) annotation.getValue();
						System.out.println("Missing Equivalent Class: "+term);
						System.out.println("Class: "+cls + " -> " + val.getLiteral());
						termInfo.add(val.getLiteral());
					}
				}
				
				// Get parent IRI and term label 
				Set<OWLClassExpression> superClasses = cls.getSuperClasses(ontology);
				System.out.println("SuperClasses: "+superClasses);
				// For each superClass, get the term label if the superClass is not anonymous
				for (OWLClassExpression superCls :  superClasses) {
					if (!superCls.isAnonymous()) {
						System.out.println("SuperClass is not Anonymous");
						termInfo.add(superCls.toString());
						for (OWLAnnotation annotation : ((OWLEntity) superCls).getAnnotations(ontology, label)) {
							if (annotation.getValue() instanceof OWLLiteral) {
								OWLLiteral val = (OWLLiteral) annotation.getValue();
								System.out.println("SuperClass: "+superCls + " -> " + val.getLiteral());
								termInfo.add(val.getLiteral());	
							}
						}
					}
					else {
						System.out.println("SuperClass IS Anonymous");
					}
					System.out.println();
				}
				
				
			
			}
		}
		return termInfo;
	}
}


