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
	 * NOTE: Some GO files on the web use update NIF IRIs (http://uri.neuinfo.org/nif/nifstd/someNumber) 
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
		IRI ONTOLOGY = IRI.create("http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Subcellular.owl"); // IRIs not updated, use with NIF-GO-CC-Bridge.owl only!
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(ONTOLOGY); 

		// Load ontology from local file
		/*File file = new File("/Users/whetzel/Documents/workspace/OntologyFiles/NIF-Subcellular-ORIG.owl");  //updated IRIs, use with go-nifstd-bridge.owl only
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
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
		IRI NIF_GO_CC_BRIDGE_ONTOLOGY = IRI.create("http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-GO-CC-Bridge.owl"); //Use with NIF-Subcellular.owl from web so IRI formats match 
		IRI GO_NIFSTD_BRIDGE_ONTOLOGY = IRI.create("http://geneontology.org/ontology/extensions/go-nifstd-bridge.owl"); // Compare to Local NIF-Subcellular.owl file with updated IRIs
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(NIF_GO_CC_BRIDGE_ONTOLOGY); 
		 
		System.out.println("Loaded ontology: " + ontology);
		return ontology;
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
				System.out.println ("EQUIVCLASSES: "+bridgeOntology.getEquivalentClassesAxioms(owlClass));
				System.out.println("All looks good\n");
			}
		}	
		// Write out results to file 
		writeFile(noEquivClass);
	}


	/**
	 * Write content to output file
	 * @param noEquivClass 
	 */
	public static void writeFile(ArrayList<String> noEquivClass) {
		try {
			//TODO Pass output file name as parameter  
			File file = new File("./noEquivClassList.txt");
			int size = noEquivClass.size();

			// If file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Total number of Classes WITHOUT an Equivalent Axiom: "+size+"\n");
			for (String term : noEquivClass) {
				bw.write(term+"\n");
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}


