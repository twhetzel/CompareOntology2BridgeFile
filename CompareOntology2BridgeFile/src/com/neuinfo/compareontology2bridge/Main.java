package com.neuinfo.compareontology2bridge;

import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
		// Load ontology from web
		//TODO Pass ontology location as a command-line argument 
		IRI ONTOLOGY = IRI.create("http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Subcellular.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(ONTOLOGY); 

		// Load ontology from local file
		/**File file = new File("/Users/whetzel/Desktop/NIF-Subcellular.owl");
        OWLOntology localOntology = manager.loadOntologyFromOntologyDocument(file);
		 */
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
		ArrayList<String> terms = new ArrayList<String>();
		System.out.println("Total number of classes: "+size);
//		for (OWLClass owlClass : allClasses) {
//			System.out.println(owlClass);
//
//			// Get IRI Fragment
//			String iri = owlClass.getIRI().getFragment();
//			//System.out.println("OWLCLASS IRI: "+iri+"\n");
//			terms.add(iri);
//		}	
		return allClasses;
	}


	/**
	 * Load ontology Bridge file
	 * @return 
	 * @throws OWLOntologyCreationException 
	 */
	private static OWLOntology loadBridgeFile() throws OWLOntologyCreationException {
		// Load ontology from web
		//TODO Pass ontology location as a command-line argument 
		IRI ONTOLOGY = IRI.create("http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-GO-CC-Bridge.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(ONTOLOGY); 

		// Load ontology from local file
		/**File file = new File("/Users/whetzel/Desktop/NIF-Subcellular.owl");
		        OWLOntology localOntology = manager.loadOntologyFromOntologyDocument(file);
		 */
		System.out.println("Loaded ontology: " + ontology);
		return ontology;
	}


	/**
	 * Get all classes in Bridge file
	 * @param manager
	 * @param ontology
	 */
	private static void getBridgeClasses(OWLOntology ontology) {
		Set<OWLClass> allClasses = ontology.getClassesInSignature();
		int size = allClasses.size();
		ArrayList<String> terms = new ArrayList<String>();
		System.out.println("Total number of classes: "+size);
		for (OWLClass owlClass : allClasses) {
			System.out.println(owlClass);
			//ontology.getEquivalentClassesAxioms(owlClass);
			//System.out.println("EquivClasses: "+ontology.getEquivalentClassesAxioms(owlClass));
			// Get IRI Fragment
			String iri = owlClass.getIRI().getFragment();
			System.out.println("OWLCLASS IRI: "+iri+"\n");
			terms.add(iri);
		}	
	}

	
	/**
	 * Check for Ontology Class in Bridge ontology file
	 * @param ontology
	 * @param bridgeOntology
	 */
	private static void searchForClassInBridgeOntology(Set<OWLClass> allClasses,
			OWLOntology bridgeOntology) {
		// Iterate through Ontology
		int size = allClasses.size();
		ArrayList<String> terms = new ArrayList<String>();
		
		//Set<OWLClass> noEquivalentClass = new Set<OWLClass>();
		
		System.out.println("Total number of classes: "+size);
		ArrayList<String> noEquivClass = new ArrayList<String>();
		
		for (OWLClass owlClass : allClasses) {
			//System.out.println("OWLCLASSES: "+owlClass); 

			bridgeOntology.getEquivalentClassesAxioms(owlClass);
			System.out.println("OWLCLASS: "+owlClass);
			//System.out.println ("EQUIVCLASSES: "+bridgeOntology.getEquivalentClassesAxioms(owlClass));
			
			if (bridgeOntology.getEquivalentClassesAxioms(owlClass).isEmpty()) {
				noEquivClass.add(owlClass.toString());
				
				// Get IRI Fragment - alternative approach
				String iri = owlClass.getIRI().getFragment();
				System.out.println("NO EQUIV CLASS: "+"("+owlClass+")"+"\n");
				terms.add(iri);
			}
			else {
				System.out.println ("EQUIVCLASSES: "+bridgeOntology.getEquivalentClassesAxioms(owlClass));
				System.out.println("All looks good\n");
			}
		}	
		writeFile(noEquivClass);
		//return ; 
	}

	
	/**
	 * Write content to output file
	 * @param noEquivClass 
	 */
	public static void writeFile(ArrayList<String> noEquivClass) {
		try {
			File file = new File("./noEquivClassList.txt");
			int size = noEquivClass.size();
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Total number of Classes with an Equivalent Axiom: "+size+"\n");
			for (String term : noEquivClass) {
				//bw.write(noEquivClass.toString());
				bw.write(term+"\n");
			}
			bw.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}


