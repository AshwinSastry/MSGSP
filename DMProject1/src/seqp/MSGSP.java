package seqp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.DataParser;
import data.DataParser.Item;
import data.DataParser.ItemSet;
import data.DataParser.Sequence;

public class MSGSP {

	DataParser m_Dparser = null;
	HashMap<Integer, Integer> m_ItmCountMap = null;
	
	/**
	 * Default constructor
	 */
	public MSGSP() {
	
		if(m_Dparser == null)
			m_Dparser = new DataParser();
	
		//load the data
		m_Dparser.parseDataFile("./data/data.txt");
		m_Dparser.parseMIS("./data/para.txt");
	}
	
	/**
	 * 
	 * @param X
	 * @param Y
	 * @return
	 */
	double getConfidence(double X, double Y){
		double conf = 0.0;
		conf = (X+Y)/X;
		return conf;
	}
	
	/**
	 *  Returns the support
	 * @param X
	 * @param Y
	 * @param n
	 * @return
	 */
	double getSupportCount(double X, double Y, double n){
		double sup = 0.0;
		sup = (X+Y)/n;
		return sup;
	}
	
	private double getMinSup(){
		double m = -1;
		
		//invalid min sup
		if(m_Dparser == null)
			return -1;
		
		//Itm, MIS key value pair
		Map<Integer, Double> itmMISmap = m_Dparser.getM_ItmMISMap();
		
		//sort the list
		List<Double> MISList = new ArrayList<Double>(itmMISmap.values());
		Collections.sort(MISList);
		
		//get the first element which shall be the minimum
		m = MISList.get(0);
		return m;
	}
	
	/**
	 * Generate L for generation of C2
	 * @param listOfSeq
	 * @return
	 */
	public List<Integer> initpass(){
		
		List<Integer> L = new ArrayList<Integer>();
		
		//sort and get the min sup
		List<Sequence> listOfSeq = m_Dparser.getM_ListofSequences();
		double m = getMinSup();
		
		//save the counts
		m_ItmCountMap = new HashMap<Integer, Integer>();
		
		for(int i = 0; i < listOfSeq.size(); i++){
			//get the sequence and its itemsets(elements)
			Sequence seq = listOfSeq.get(i);
			List<ItemSet> isList =  seq.getM_ItmSet();
			
			//iterate over all the elements of a sequence
			for(int j = 0; j < isList.size(); j++){
				ItemSet is = isList.get(j);
				
				//get all the items in a itemset
				Set<Item> itms = is.getM_ItemSet();
				Iterator<Item> itr = itms.iterator();
				
				while(itr.hasNext()){
					Item itm = itr.next();
					int itmValue = itm.getItem();
					if(m_ItmCountMap.containsKey(itmValue))
						m_ItmCountMap.put(itmValue, m_ItmCountMap.get(itmValue)+1);
					else
						m_ItmCountMap.put(itmValue, 1);
				}
			}
		}
		
		//only insert Item which has support count >= minsup
		Set<Integer> all = m_ItmCountMap.keySet();
		Iterator<Integer> allItr = all.iterator();
		while(allItr.hasNext()){
			int key = allItr.next();
			//get the count and calculate support
			int count = m_ItmCountMap.get(key);
			double sup = getSupportCount(count, 0, listOfSeq.size());
			if( sup >= m){
				L.add(key);
			}
		}
		return L;
	}
	
	/**
	 * Return Frequent 1-sequence pattern
	 * @return
	 */
	List<Sequence> generateF1(List<Integer> L){

		List<Sequence> listOfF1 = new ArrayList<DataParser.Sequence>();
		List<Sequence> listOfSeq = m_Dparser.getM_ListofSequences();
		
		//iterate over the counts
		if(m_ItmCountMap != null){
			
			Set<Integer> all = m_ItmCountMap.keySet();
			Iterator<Integer> allItr = all.iterator();
			
			while(allItr.hasNext()){
				int key = allItr.next();
				
				//get the count and calculate support
				int count = m_ItmCountMap.get(key);
				double sup = getSupportCount(count, 0, listOfSeq.size());
				
				//Item belongs to L and sup >= MIS(Item)
				if(L.contains(key) && sup >= m_Dparser.getM_ItmMISMap().get(key)){
					ItemSet is = m_Dparser.new ItemSet();
					is.addItem(m_Dparser.new Item(key));
					Sequence seq = m_Dparser.new Sequence();
					seq.addItmSet(is);
					
					//add to the list
					listOfF1.add(seq);
				}
			}
		}		
		return listOfF1;
	}
	
	void displaySequence(List<Sequence> listOfSeq){
		
		if(listOfSeq == null)
			return;
		
		for(int i = 0; i < listOfSeq.size(); i++){
			
			//get the sequence and its itemsets(elements)
			Sequence seq = listOfSeq.get(i);
			List<ItemSet> isList =  seq.getM_ItmSet();
			
			System.out.print("<");
			//iterate over all the elements of a sequence
			for(int j = 0; j < isList.size(); j++){
				ItemSet is = isList.get(j);
				
				//get all the items in a itemset
				Set<Item> itms = is.getM_ItemSet();
				Iterator<Item> itr = itms.iterator();
				
				System.out.print("{");
				while(itr.hasNext()){
					Item itm = itr.next();
					System.out.print(itm.getItem());
					
					if(itr.hasNext())
						System.out.print(",");
				}
				System.out.print("}");
			}
			System.out.println(">");
		}
	}
	
	List<Sequence> level2CandidateGenSPM(List<Integer> L){
		List<Sequence> C2 = new ArrayList<DataParser.Sequence>();
		
		for(int i = 0; i < L.size(); i++){
			int itm1 = L.get(i);
			int itm1Count = m_ItmCountMap.get(itm1);
			Double itm1Sup = getSupportCount(itm1Count, 0, m_Dparser.getM_ListofSequences().size());
			
			if(itm1Sup >= m_Dparser.getM_ItmMISMap().get(itm1)){
				for(int j = i+1; j < L.size();j++){
					int itm2 = L.get(j);
					int itm2Count = m_ItmCountMap.get(itm2);
					Double itm2Sup = getSupportCount(itm2Count, 0, m_Dparser.getM_ListofSequences().size());
					
					if(itm2Sup >= m_Dparser.getM_ItmMISMap().get(itm1)){
						ItemSet is = m_Dparser.new ItemSet();
						is.addItem(m_Dparser.new Item(itm1));
						is.addItem(m_Dparser.new Item(itm2));
						
						//add the itemset to seq
						Sequence seq = m_Dparser.new Sequence();
						seq.addItmSet(is);
						
						//second type of itemset
						ItemSet is21 = m_Dparser.new ItemSet();
						is21.addItem(m_Dparser.new Item(itm1));
						ItemSet is22 = m_Dparser.new ItemSet();
						is22.addItem(m_Dparser.new Item(itm2));
												
						//add the itemset to seq2
						Sequence seq2 = m_Dparser.new Sequence();
						seq2.addItmSet(is21);
						seq2.addItmSet(is22);
						
						C2.add(seq);
						C2.add(seq2);
					}
				}
			}
		}
		
		return C2;
	}
	
	private boolean FitemLessThanOth(Sequence s){
		//get the first item
		Item first = getItem(s, 1); //obv we do not have empty itemset
		
		List<ItemSet> listOfImst = s.getM_ItmSet();
		for(int i = 0; i < listOfImst.size(); i++){
			ItemSet is = listOfImst.get(i);
			Set<Item> itms = is.getM_ItemSet();
			
			Iterator<Item> itr = itms.iterator();
			
			while(itr.hasNext()){
				Item itm = itr.next();
				if(first.getItem() > itm.getItem())
					return false;
			}
		}
		
		return true;
	}
	
	private boolean LitemLessThanOth(Sequence s){
		//get the last item
		Item last = getItem(s, -1); //last item in the sequence
		
		List<ItemSet> listOfImst = s.getM_ItmSet();
		for(int i = 0; i < listOfImst.size(); i++){
			ItemSet is = listOfImst.get(i);
			Set<Item> itms = is.getM_ItemSet();
			Iterator<Item> itr = itms.iterator();
			
			while(itr.hasNext()){
				Item itm = itr.next();
				
				//no need to compare against last item
				if(!itr.hasNext() && i == listOfImst.size()-1)
					continue;
				
				//compare with all the items
				if(last.getItem() > itm.getItem() )
					return false;
			}
		}
		
		return true;
	}

	/**
	 * Drops the Item specified by place holder
	 * @param s
	 * @param itemPlaceholder
	 * @return
	 */
	private Sequence dropItem(Sequence s, int itemPlaceholder){
		int count = 0;
		List<ItemSet> listIS = s.getM_ItmSet();
		for(int i = 0; i < listIS.size(); i++){
			Set<Item> itms = listIS.get(i).getM_ItemSet();
			Iterator<Item> itr = itms.iterator();
			while(itr.hasNext()){
				count++;
				//last item in the sequence
				if(count == -1){
					if(i == listIS.size()-1 && !itr.hasNext()){
						itr.remove();
						return s;
					}
				}
				else if(count == itemPlaceholder){
					itr.remove();
					return s;
				}
			}	
		}
		return null;
	}
	
	private double getMISForItem(Item itm){
		return m_Dparser.getM_ItmMISMap().get(itm.getItem());
	}
	
	/**
	 * 
	 * @param s
	 * @param itemPlaceholder 1: first item, 2: second item and goes on.. -1 denotes last time
	 * @return
	 */
	private Item getItem(Sequence s, int itemPlaceholder){
		int count = 0;
		List<ItemSet> listIS = s.getM_ItmSet();
		for(int i = 0; i < listIS.size(); i++){
			Set<Item> itms = listIS.get(i).getM_ItemSet();
			Iterator<Item> itr = itms.iterator();
			while(itr.hasNext()){
				count++;
				Item itm = itr.next();
				
				//last item in the sequence
				if(count == -1){
					if(i == listIS.size()-1 && !itr.hasNext())
						return itm;
				}
				else if(count == itemPlaceholder){
					return itm;
				}
			}	
		}
		return null;
	}
	
	/**
	 * Returns the size of the sequence s
	 * @param s
	 * @return
	 */
	private int getSizeOfSequence(Sequence s){
		return s == null ? -1 : s.getM_ItmSet().size();
	}
	
	/**
	 * Returns the length of the sequence s
	 * @param s
	 * @return
	 */
	private int getLengthOfSequence(Sequence s){
		int length = 0;
		List<ItemSet> listIS = s.getM_ItmSet();
		for(int i = 0; i < listIS.size(); i++){
			Set<Item> itms = listIS.get(i).getM_ItemSet();
			Iterator<Item> itr = itms.iterator();
			while(itr.hasNext()){
				length++;
			}
		}
		return length;
	}
			
	List<Sequence> MSCandidateGenSPM(List<Sequence> Fk_minus_1){
		List<Sequence> Fk = new ArrayList<DataParser.Sequence>();
		
		//join step
		for(int i = 0; i < Fk_minus_1.size(); i++){
			Sequence seq1 = Fk_minus_1.get(i);
			for(int j = 0; j < Fk_minus_1.size(); j++){
				Sequence seq2 = Fk_minus_1.get(j);
				
				//condition one
				if(FitemLessThanOth(seq1)){
					//(1)
					Sequence subseq1 = dropItem(seq1, 2);
					Sequence subseq2 = dropItem(seq2, -1);
					//(2)
					Item lastItemofseq2 = getItem(seq2, -1);
					double lastItemofseq2MIS = getMISForItem(lastItemofseq2);
					Item firstItemofseq1 = getItem(seq1, 1);
					double firstItemofseq1MIS = getMISForItem(firstItemofseq1);
					
					//extending s1 with last item of s2 
					if(subseq1.equals(subseq2) && lastItemofseq2MIS > firstItemofseq1MIS){

						//if last item 'l' in s2 is a separate element
						ItemSet lastIS = seq2.getM_ItmSet().get(seq2.getM_ItmSet().size());
						int sizeofs1 = getSizeOfSequence(seq1);
						int lenofs1 = getLengthOfSequence(seq1);
						Item lastItemofseq1 = getItem(seq1, -1);
						if(lastIS.getM_ItemSet().size() == 1 && lastIS.getM_ItemSet().equals(lastItemofseq2)){

							//first candidate sequence
							Sequence c1 = seq1;
							ItemSet newis = m_Dparser.new ItemSet();
							newis.addItem(m_Dparser.new Item(lastItemofseq2.getItem()));
							c1.addItmSet(newis);
							//add the candidate sequence
							Fk.add(c1);

							//len and size of s1 is 2 and last item of s2 > last item of s1
							if(sizeofs1 == 2 && lenofs1 == 2 && lastItemofseq2.getItem() > lastItemofseq1.getItem()){
								ItemSet oldis = seq1.getM_ItmSet().get(seq1.getM_ItmSet().size()-1); //last itemset
								oldis.addItem(m_Dparser.new Item(lastItemofseq2.getItem()));

								//second candidate sequence
								Sequence c2 = seq1;
								c2.addItmSet(oldis);

								//add the candidate sequence
								Fk.add(c2);
							}
						}
						
						else if(sizeofs1 == 1 && lenofs1 == 2 && 
								(lastItemofseq2.getItem() > lastItemofseq1.getItem() ||
										lenofs1 > 2)){
							
							ItemSet oldis = seq1.getM_ItmSet().get(seq1.getM_ItmSet().size()-1); //last itemset
							oldis.addItem(m_Dparser.new Item(lastItemofseq2.getItem()));

							//second candidate sequence
							Sequence c2 = seq1;

							//add the candidate sequence
							Fk.add(c2);
						}
					}
				}
				
				//condition two
				if(LitemLessThanOth(seq2)){
					//need to implement
				}
				
				else{ //condition three
					Sequence subseq1 = dropItem(seq1, 1);
					Sequence subseq2 = dropItem(seq2, -1);
					Item lastItemofseq2 = getItem(seq2, -1);					
					
					//extending s1 with last item of s2 
					if(subseq1.equals(subseq2)){
						//if last item 'l' in s2 is a separate element
						ItemSet lastIS = seq2.getM_ItmSet().get(seq2.getM_ItmSet().size());
						if(lastIS.getM_ItemSet().size() == 1 && lastIS.getM_ItemSet().equals(lastItemofseq2)){
							//first candidate sequence
							Sequence c1 = seq1;
							ItemSet newis = m_Dparser.new ItemSet();
							newis.addItem(m_Dparser.new Item(lastItemofseq2.getItem()));
							c1.addItmSet(newis);
							//add the candidate sequence
							Fk.add(c1);
						}
						else{
							ItemSet oldis = seq1.getM_ItmSet().get(seq1.getM_ItmSet().size()-1); //last itemset
							oldis.addItem(m_Dparser.new Item(lastItemofseq2.getItem()));

							//second candidate sequence
							Sequence c2 = seq1;
							
							//add the candidate sequence
							Fk.add(c2);
						}			
					}
				}
				
			}
		}
		
		return Fk;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {	
		MSGSP msgsp = new MSGSP();
		List<Integer> L = msgsp.initpass();
		List<Sequence> F1 = msgsp.generateF1(L);
		msgsp.displaySequence(F1);
		List<Sequence> l2 = msgsp.level2CandidateGenSPM(L);
		msgsp.displaySequence(l2);
	}

}
