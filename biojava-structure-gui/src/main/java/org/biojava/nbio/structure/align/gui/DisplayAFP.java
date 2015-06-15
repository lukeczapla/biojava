/*
 *                    PDB web development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 *
 * Created on Jun 13, 2009
 * Created by Andreas Prlic
 *
 */

package org.biojava.nbio.structure.align.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.ChainImpl;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureImpl;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.gui.aligpanel.AligPanel;
import org.biojava.nbio.structure.align.gui.aligpanel.MultipleAligPanel;
import org.biojava.nbio.structure.align.gui.aligpanel.MultipleStatusDisplay;
import org.biojava.nbio.structure.align.gui.aligpanel.StatusDisplay;
import org.biojava.nbio.structure.align.gui.jmol.AbstractAlignmentJmol;
import org.biojava.nbio.structure.align.gui.jmol.JmolTools;
import org.biojava.nbio.structure.align.gui.jmol.StructureAlignmentJmol;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.multiple.Block;
import org.biojava.nbio.structure.align.multiple.BlockSet;
import org.biojava.nbio.structure.align.multiple.MultipleAlignment;
import org.biojava.nbio.structure.align.multiple.MultipleAlignmentTools;
import org.biojava.nbio.structure.align.multiple.StructureAlignmentException;
import org.biojava.nbio.structure.align.util.AFPAlignmentDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A utility class for visualistion of structure alignments
 * 
 * @author Andreas Prlic
 *
 */
public class DisplayAFP {

	private static final Logger logger = LoggerFactory.getLogger(DisplayAFP.class);
	
	//TODO: same as getEqrPos??? !!!
	public static final List<Integer> getEQRAlignmentPos(AFPChain afpChain){
		List<Integer> lst = new ArrayList<Integer>();

		char[] s1 = afpChain.getAlnseq1();
		char[] s2 = afpChain.getAlnseq2();
		char[] symb = afpChain.getAlnsymb();
		boolean isFatCat = afpChain.getAlgorithmName().startsWith("jFatCat");

		for ( int i =0 ; i< s1.length; i++){
			char c1 = s1[i];
			char c2 = s2[i];

			if ( isAlignedPosition(i,c1,c2,isFatCat, symb)) {
				lst.add(i);			  
			}

		}
		return lst;

	}





	private static boolean isAlignedPosition(int i, char c1, char c2, boolean isFatCat,char[]symb)
	{
//		if ( isFatCat){
			char s = symb[i];
			if ( c1 != '-' && c2 != '-' && s != ' '){
				return true;
			}          
//		} else {
//
//			if ( c1 != '-' && c2 != '-')
//				return true;
//		}

		return false;


	}

	/**
	 * Return a list of pdb Strings corresponding to the aligned positions of the molecule. 
	 * Only supports a pairwise alignment with the AFPChain DS.
	 * 
	 * @param aligPos
	 * @param afpChain
	 * @param ca
	 */
	public static final List<String> getPDBresnum(int aligPos, AFPChain afpChain, Atom[] ca){
		List<String> lst = new ArrayList<String>();
		if ( aligPos > 1) {
			System.err.println("multiple alignments not supported yet!");
			return lst;
		}	

		int blockNum = afpChain.getBlockNum();      
		int[] optLen = afpChain.getOptLen();
		int[][][] optAln = afpChain.getOptAln();

		if ( optLen == null)
			return lst;

		for(int bk = 0; bk < blockNum; bk ++)       {

			for ( int i=0;i< optLen[bk];i++){

				int pos = optAln[bk][aligPos][i];
				if ( pos < ca.length) {
					String pdbInfo = JmolTools.getPdbInfo(ca[pos]);
					//lst.add(ca1[pos].getParent().getPDBCode());
					lst.add(pdbInfo);
				}
			}

		}
		return lst;
	}
	
	/**
	 * Return a list of pdb Strings corresponding to the aligned positions of the molecule.
	 * <p> 
	 * Adapted the method to a more general version for the multiple alignments, 
	 * using the MultipleAlignment DS.
	 * 
	 * @param aligPos
	 * @param multAln
	 * @param ca
	 */
	public static final List<String> getPDBresnum(int structNum, MultipleAlignment multAln, Atom[] ca) {
		
		List<String> lst = new ArrayList<String>();

		//Loop through all the BlockSets
		for( BlockSet bs : multAln.getBlockSets() ) {
			
			//Loop through all the Blocks
			for (Block block : bs.getBlocks() ){
			
				//Loop though all the residues in the Block
				for (int i=0; i<block.length(); i++){
					Integer pos = block.getAlignRes().get(structNum).get(i);
					if (pos==null) continue; //It means a GAP
					else if (pos < ca.length) {
						String pdbInfo = JmolTools.getPdbInfo(ca[pos]);
						lst.add(pdbInfo);
					}
				}
			}
		}
		return lst;
	}

	/** get the block number for an aligned position
	 * 
	 * @param afpChain
	 * @param aligPos
	 * @return
	 * @deprecated use AFPAlignmentDisplay.getBlockNrForAlignPos instead...
	 */
	@Deprecated
	public static int getBlockNrForAlignPos(AFPChain afpChain, int aligPos){
		return AFPAlignmentDisplay.getBlockNrForAlignPos(afpChain, aligPos);
	}



	/** return the atom at alignment position aligPos. at the present only works with block 0
	 * @param chainNr the number of the aligned pair. 0... first chain, 1... second chain.
	 * @param afpChain an afpChain object
	 * @param aligPos position on the alignment
	 * @param getPrevious gives the previous position if false, gives the next posible atom
	 * @return a CA atom that is at a particular position of the alignment 
	 */
	public static final Atom getAtomForAligPos(AFPChain afpChain,int chainNr, int aligPos, Atom[] ca , boolean getPrevious ) throws StructureException{
		int[] optLen = afpChain.getOptLen();
		// int[][][] optAln = afpChain.getOptAln();

		if ( optLen == null)
			return null;

		if (chainNr < 0 || chainNr > 1){
			throw new StructureException("So far only pairwise alignments are supported, but you requested results for alinged chain nr " + chainNr);
		}

		//if (  afpChain.getAlgorithmName().startsWith("jFatCat")){

		/// for FatCat algorithms...
		int capos = getUngappedFatCatPos(afpChain, chainNr, aligPos);
		if ( capos < 0) {

			capos = getNextFatCatPos(afpChain, chainNr, aligPos,getPrevious);

			//System.out.println(" got next" + capos + " for " + chainNr + " alignedPos: " + aligPos);
		} else {
			//System.out.println("got aligned fatcat position: " + capos + " " + chainNr + " for alig pos: " + aligPos);	
		}

		if ( capos < 0) {
			System.err.println("could not match position " + aligPos + " in chain " + chainNr +". Returing null...");
			return null;
		}
		if ( capos > ca.length){
			System.err.println("Atom array "+ chainNr + " does not have " + capos +" atoms. Returning null.");
			return null;
		}
		return ca[capos];
		//}

		//    
		//      
		//      int ungappedPos = getUngappedPos(afpChain, aligPos);
		//      System.out.println("getAtomForAligPOs " + aligPos  + " " + ungappedPos );
		//      return ca[ungappedPos];
		//      
		//      if ( ungappedPos >= optAln[bk][chainNr].length)
		//         return null;
		//      int pos = optAln[bk][chainNr][ungappedPos];
		//      if ( pos > ca.length)
		//         return null;
		//      return ca[pos];
	}


	private static int getNextFatCatPos(AFPChain afpChain, int chainNr,
			int aligPos, boolean getPrevious) {

		char[] aseq;
		if ( chainNr == 0 )
			aseq = afpChain.getAlnseq1();
		else
			aseq = afpChain.getAlnseq2();

		if ( aligPos > aseq.length)
			return -1;
		if ( aligPos < 0)
			return -1;

		int blockNum = afpChain.getBlockNum();
		int[] optLen = afpChain.getOptLen();
		int[][][] optAln = afpChain.getOptAln();

		int p1, p2;
		int p1b = 0;
		int p2b = 0;
		int len = 0;



		boolean terminateNextMatch = false;
		for(int i = 0; i < blockNum; i ++)  {        	
			for(int j = 0; j < optLen[i]; j ++) {

				p1 = optAln[i][0][j];
				p2 = optAln[i][1][j];


				if(len > 0)     {

					int lmax = (p1 - p1b - 1)>(p2 - p2b - 1)?(p1 - p1b - 1):(p2 - p2b - 1);

					// lmax gives the length of an alignment gap

					//System.out.println("  pos "+ len+" p1-p2: " + p1 + " - " + p2 + " lmax: " + lmax + " p1b-p2b:"+p1b + " " + p2b + " terminate? "+ terminateNextMatch);	
					for(int k = 0; k < lmax; k ++)      {

						if(k >= (p1 - p1b - 1)) {
							// a gap position in chain 0
							if ( aligPos == len && chainNr == 0 ){
								if ( getPrevious)
									return p1b;
								else
									terminateNextMatch = true;
							}
						}
						else {
							if ( aligPos == len && chainNr == 0)
								return p1b+1+k;


						}
						if(k >= (p2 - p2b - 1)) {
							// a gap position in chain 1
							if ( aligPos == len && chainNr == 1){
								if ( getPrevious)
									return p2b;
								else 
									terminateNextMatch = true;
							}
						}
						else  {
							if ( aligPos == len && chainNr == 1) {
								return p2b+1+k;
							}


						}
						len++;

					}
				}

				if ( aligPos == len && chainNr == 0)
					return p1;
				if ( aligPos == len && chainNr == 1)
					return p2;



				if ( terminateNextMatch)
					if ( chainNr == 0)
						return p1;
					else 
						return p2;
				if ( len > aligPos) {
					if ( getPrevious) {
						if ( chainNr == 0)
							return p1b;
						else
							return p2b;
					} else {
						terminateNextMatch = true;
					}
				}

				len++;
				p1b = p1;
				p2b = p2;




			}
		}


		// we did not find an aligned position
		return -1;

	}

	private static final int getUngappedFatCatPos(AFPChain afpChain, int chainNr, int aligPos){
		char[] aseq;
		if ( chainNr == 0 )
			aseq = afpChain.getAlnseq1();
		else
			aseq = afpChain.getAlnseq2();

		if ( aligPos > aseq.length)
			return -1;
		if ( aligPos < 0)
			return -1;

		int blockNum = afpChain.getBlockNum();
		int[] optLen = afpChain.getOptLen();
		int[][][] optAln = afpChain.getOptAln();

		int p1, p2;
		int p1b = 0;
		int p2b = 0;
		int len = 0;


		for(int i = 0; i < blockNum; i ++)  {        	
			for(int j = 0; j < optLen[i]; j ++) {

				p1 = optAln[i][0][j];
				p2 = optAln[i][1][j];


				if(len > 0)     {

					int lmax = (p1 - p1b - 1)>(p2 - p2b - 1)?(p1 - p1b - 1):(p2 - p2b - 1);

					// lmax gives the length of an alignment gap

					//System.out.println("   p1-p2: " + p1 + " - " + p2 + " lmax: " + lmax + " p1b-p2b:"+p1b + " " + p2b);	
					for(int k = 0; k < lmax; k ++)      {

						if(k >= (p1 - p1b - 1)) {
							// a gap position in chain 0
							if ( aligPos == len && chainNr == 0){
								return -1;
							}
						}
						else {
							if ( aligPos == len && chainNr == 0)
								return p1b+1+k;


						}
						if(k >= (p2 - p2b - 1)) {
							// a gap position in chain 1
							if ( aligPos == len && chainNr == 1){
								return -1;
							}
						}
						else  {
							if ( aligPos == len && chainNr == 1) {
								return p2b+1+k;
							}


						}
						len++;

					}
				}

				if ( aligPos == len && chainNr == 0)
					return p1;
				if ( aligPos == len && chainNr == 1)
					return p2;

				len++;
				p1b = p1;
				p2b = p2;


			}
		}


		// we did not find an aligned position
		return -1;
	}


	/** get an artifical List of chains containing the Atoms and groups.
	 * Does NOT rotate anything.
	 * @param ca
	 * @return a list of Chains that is built up from the Atoms in the ca array
	 * @throws StructureException
	 */
	private static final List<Chain> getAlignedModel(Atom[] ca){

		List<Chain> model = new ArrayList<Chain>();
		for ( Atom a: ca){

			Group g = a.getGroup();
			Chain parentC = g.getChain();

			Chain newChain = null;
			for ( Chain c :  model) {
				if ( c.getChainID().equals(parentC.getChainID())){
					newChain = c;
					break;
				}
			}
			if ( newChain == null){

				newChain = new ChainImpl();

				newChain.setChainID(parentC.getChainID());

				model.add(newChain);
			}

			newChain.addGroup(g);

		}

		return model;
	}


	/** Get an artifical Structure containing both chains.
	 * Does NOT rotate anything
	 * @param ca1
	 * @param ca2
	 * @return a structure object containing two models, one for each set of Atoms.
	 * @throws StructureException
	 */
	public static final Structure getAlignedStructure(Atom[] ca1, Atom[] ca2) throws StructureException{

		/* Previous implementation commented

		Structure s = new StructureImpl();


		List<Chain>model1 = getAlignedModel(ca1);
		List<Chain>model2 = getAlignedModel(ca2);
		s.addModel(model1);
		s.addModel(model2);
		
		return s;*/

		return getAlignedStructure(Arrays.asList(ca1,ca2));
	}
	
	/** Get an artifical Structure containing all the chains (Multiple Alignment generalization)
	 * Does NOT rotate anything
	 * @param ca1
	 * @param ca2
	 * @return a structure object containing two models, one for each set of Atoms.
	 * @throws StructureException
	 */
	public static final Structure getAlignedStructure(List<Atom[]> atomArrays) throws StructureException{

		Structure s = new StructureImpl();

		for (int i=0; i<atomArrays.size(); i++){
			List<Chain>model = getAlignedModel(atomArrays.get(i));
			s.addModel(model);
		}

		return s;
	}



	/**
	 * Returns the first atom for each group
	 * @param ca
	 * @param hetatms
	 * @return
	 * @throws StructureException
	 */
	public static final Atom[] getAtomArray(Atom[] ca,List<Group> hetatms ) throws StructureException{
		List<Atom> atoms = new ArrayList<Atom>();
		Collections.addAll(atoms, ca);

		logger.debug("got {} hetatoms", hetatms.size());
		
		// we only add atom nr 1, since the getAlignedStructure method actually adds the parent group, and not the atoms...
		for (Group g : hetatms){
			if (g.size() < 1)
				continue;
			//if (debug)
			//   System.out.println("adding group " + g);
			Atom a = g.getAtom(0);
			//if (debug)
			//  System.out.println(a);
			a.setGroup(g);
			atoms.add(a);
		}

		Atom[] arr = atoms.toArray(new Atom[atoms.size()]);

		return arr;
	}


	/** Note: ca2, hetatoms2 and nucleotides2 should not be rotated. This will be done here...
	 * */

	public static final StructureAlignmentJmol display(AFPChain afpChain,Group[] twistedGroups, Atom[] ca1, Atom[] ca2,List<Group> hetatms1, List<Group> hetatms2 ) throws StructureException {

		List<Atom> twistedAs = new ArrayList<Atom>();

		for ( Group g: twistedGroups){
			if ( g == null )
				continue;
			if ( g.size() < 1)
				continue;
			Atom a = g.getAtom(0);
			twistedAs.add(a);
		}
		Atom[] twistedAtoms = twistedAs.toArray(new Atom[twistedAs.size()]);
		twistedAtoms = StructureTools.cloneAtomArray(twistedAtoms);

		Atom[] arr1 = getAtomArray(ca1, hetatms1);
		Atom[] arr2 = getAtomArray(twistedAtoms, hetatms2);

		// 

		//if ( hetatms2.size() > 0)
			//	System.out.println("atom after:" + hetatms2.get(0).getAtom(0));

		//if ( hetatms2.size() > 0)
		//	System.out.println("atom after:" + hetatms2.get(0).getAtom(0));

		String title =  afpChain.getAlgorithmName() + " V." +afpChain.getVersion() + " : " + afpChain.getName1() + " vs. " + afpChain.getName2();

		//System.out.println(artificial.toPDB());



		StructureAlignmentJmol jmol = new StructureAlignmentJmol(afpChain,arr1,arr2);      
		//jmol.setStructure(artificial);

		System.out.format("CA2[0]=(%.2f,%.2f,%.2f)%n", arr2[0].getX(), arr2[0].getY(), arr2[0].getZ());

		//jmol.setTitle("Structure Alignment: " + afpChain.getName1() + " vs. " + afpChain.getName2());
		jmol.setTitle(title);
		return jmol;
	}

	public static void showAlignmentImage(AFPChain afpChain, Atom[] ca1, Atom[] ca2, AbstractAlignmentJmol jmol) throws StructureException {
		
		AligPanel me = new AligPanel();
		me.setAlignmentJmol(jmol);
		me.setAFPChain(afpChain);
		me.setCa1(ca1);
		me.setCa2(ca2);

		JFrame frame = new JFrame();

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		frame.setTitle(afpChain.getName1() + " vs. " + afpChain.getName2() + " | " + afpChain.getAlgorithmName() + " V. " + afpChain.getVersion());
		me.setPreferredSize(new Dimension(me.getCoordManager().getPreferredWidth() , me.getCoordManager().getPreferredHeight()));

		JMenuBar menu = MenuCreator.getAlignmentTextMenu(frame,me,afpChain);
		frame.setJMenuBar(menu);

		JScrollPane scroll = new JScrollPane(me);
		scroll.setAutoscrolls(true);

		StatusDisplay status = new StatusDisplay();
		status.setAfpChain(afpChain);
		status.setCa1(ca1);
		status.setCa2(ca2);
		me.addAlignmentPositionListener(status);

		Box vBox = Box.createVerticalBox();
		vBox.add(scroll);
		vBox.add(status);

		frame.getContentPane().add(vBox);

		frame.pack();
		frame.setVisible(true);
		// make sure they get cleaned up correctly:
		frame.addWindowListener(me);
		frame.addWindowListener(status);
	}
	
	public static void showAlignmentImage(MultipleAlignment multAln, AbstractAlignmentJmol jmol, Color[] colors) throws StructureAlignmentException, StructureException {
		
		MultipleAligPanel me = new MultipleAligPanel(multAln, colors, jmol);
		JFrame frame = new JFrame();

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		frame.setTitle("Alignment Panel for Multiple Structure Alignments - alpha version");
		me.setPreferredSize(new Dimension(me.getCoordManager().getPreferredWidth() , me.getCoordManager().getPreferredHeight()));

		JMenuBar menu = MenuCreator.getAlignmentTextMenu(frame,me,null);
		frame.setJMenuBar(menu);

		JScrollPane scroll = new JScrollPane(me);
		scroll.setAutoscrolls(true);

		MultipleStatusDisplay status = new MultipleStatusDisplay(me);
		me.addAlignmentPositionListener(status);

		Box vBox = Box.createVerticalBox();
		vBox.add(scroll);
		vBox.add(status);
		frame.getContentPane().add(vBox);

		frame.pack();
		frame.setVisible(true);
		//make sure they get cleaned up correctly:
		frame.addWindowListener(me);
		frame.addWindowListener(status);
	}

	public static void showAlignmentImage(AFPChain afpChain, String result) {

		JFrame frame = new JFrame();

		String title = afpChain.getAlgorithmName() + " V."+afpChain.getVersion() + " : " + afpChain.getName1()  + " vs. " + afpChain.getName2() ;
		frame.setTitle(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		AlignmentTextPanel txtPanel = new AlignmentTextPanel();
		txtPanel.setText(result);

		JMenuBar menu = MenuCreator.getAlignmentTextMenu(frame,txtPanel,afpChain);

		frame.setJMenuBar(menu);
		JScrollPane js = new JScrollPane();
		js.getViewport().add(txtPanel);
		js.getViewport().setBorder(null);
		//js.setViewportBorder(null);
		//js.setBorder(null);
		//js.setBackground(Color.white);

		frame.getContentPane().add(js);
		frame.pack();      
		frame.setVisible(true);

	}
	
	/** Create a "fake" Structure objects that contains the two sets of atoms aligned on top of each other.
	 * 
	 * @param afpChain the container of the alignment
	 * @param ca1 atoms for protein 1
	 * @param ca2 atoms for protein 2
	 * @return a protein structure with 2 models.
	 * @throws StructureException
	 */
	public static Structure createArtificalStructure(AFPChain afpChain, Atom[] ca1,
			Atom[] ca2) throws StructureException{

		
		if ( afpChain.getNrEQR() < 1){
			return DisplayAFP.getAlignedStructure(ca1, ca2);
		}
		
		Group[] twistedGroups = StructureAlignmentDisplay.prepareGroupsForDisplay(afpChain,ca1, ca2);

		List<Atom> twistedAs = new ArrayList<Atom>();

		for ( Group g: twistedGroups){
			if ( g == null )
				continue;
			if ( g.size() < 1)
				continue;
			Atom a = g.getAtom(0);
			twistedAs.add(a);
		}
		Atom[] twistedAtoms = twistedAs.toArray(new Atom[twistedAs.size()]);

		List<Group> hetatms  = StructureTools.getUnalignedGroups(ca1);
		List<Group> hetatms2 = StructureTools.getUnalignedGroups(ca2);

		Atom[] arr1 = DisplayAFP.getAtomArray(ca1, hetatms);
		Atom[] arr2 = DisplayAFP.getAtomArray(twistedAtoms, hetatms2);

		Structure artificial = DisplayAFP.getAlignedStructure(arr1,arr2);
		return artificial;
	}

	/**
	 * Given an aligned position in the sequence alignment, it returns the Atom that corresponds.
	 * It does not support gaps in the multiple alignment right now!
	 * 
	 * @param multAln the alignment information
	 * @param str the structure
	 * @param pos the position in the sequence alignment
	 * @return Atom the aligned atom
	 * @throws StructureAlignmentException if the atoms cannot be obtained
	 */
	@Deprecated
	public static Atom getAtomForAligPos(MultipleAlignment multAln, int str, int pos) throws StructureAlignmentException {
		
		List<Integer> residues = new ArrayList<Integer>();
		for (BlockSet bs:multAln.getBlockSets()){
			for (Block b:bs.getBlocks()) residues.addAll(b.getAlignRes().get(str));
		}
		if (residues.size() == 0) return null;
		
		String sequence = MultipleAlignmentTools.getSequenceAlignment(multAln).get(str);
		int aligpos = 0;
		for (int i=0; i<pos; i++){
			if (sequence.charAt(i) != '-') aligpos++;
		}
		int capos = residues.get(aligpos);

		if (capos < 0 || capos > multAln.getEnsemble().getAtomArrays().get(str).length) return null;
		return multAln.getEnsemble().getAtomArrays().get(str)[capos];
	}

	/**
	 * Returns all the positions in the alignment sequence that are aligned (do not include gaps). 
	 * The core residues for multiple alignments.
	 * 
	 * @param multAln
	 * @return List with all the aligned positions
	 * @throws StructureAlignmentException 
	 */
	@Deprecated
	public static List<Integer> getCoreAlignmentPos(MultipleAlignment multAln) throws StructureAlignmentException {
		
		List<Integer> lst = new ArrayList<Integer>();
		List<String> alnSeq = MultipleAlignmentTools.getSequenceAlignment(multAln);
		for (int i =0 ; i< alnSeq.get(0).length(); i++){
			boolean aligned = true;
			for (int str = 0; str<multAln.size(); str++){
				char c = alnSeq.get(str).charAt(i);
				if (c == '-'){
					aligned = false;
					break;
				}
			}
			if (aligned == true) {
				lst.add(i);
			}
		}
		return lst;
	}
}
