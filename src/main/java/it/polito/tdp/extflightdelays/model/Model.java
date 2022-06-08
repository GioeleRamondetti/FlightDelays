package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	private Graph<Airport,DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer,Airport> idMap;
	
	public Model() {
		dao=new ExtFlightDelaysDAO();
		idMap=new HashMap<Integer,Airport>();
		dao.loadAllAirports(idMap);
		// riempio il grafo con i vetici all inizio una sola volta
		// carico tutti gli aeroporti nella mappa
		// posso benisimo caricare solo quelli gia filtrati con x numero di compagnie
	}
	public void creaGrafo(int x) {
		grafo=new SimpleWeightedGraph<Airport,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		// aggiungo i vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(x, idMap));
		// aggiungo gli archi
		for(Rotta r: dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge edge=this.grafo.getEdge(r.getA1(), r.getA2());
				if(edge==null) {
					Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(),x);
				}else {
					// peso sempre double
					double pesovecchio=this.grafo.getEdgeWeight(edge);
					double pesonuovo=pesovecchio+r.getnVoli();
					this.grafo.setEdgeWeight(edge, pesonuovo);
				}
			}
		}
		System.out.println("vertici : "+this.grafo.vertexSet().size()+"\n");
		System.out.println("archi : "+this.grafo.edgeSet().size()+"\n");
	}
	
	public List<Airport> getVertici(){
		List<Airport> vertici =new ArrayList<>(this.grafo.vertexSet());
		Collections.sort(vertici);
		return vertici;
	}
	public List<Airport> getpercorso (Airport a1,Airport a2){
		List<Airport> percorso=new ArrayList<>();
		BreadthFirstIterator<Airport,DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo,a1);
		boolean trovato =false;
		while(it.hasNext()) {
			Airport visitato=it.next();
			if(visitato.equals(a2)) {
				trovato=true;
			}
			it.next();
		}
		if(trovato==true) {
			percorso.add(a2);
			Airport step=it.getParent(a2);
			while(!step.equals(a1)) {
				percorso.add(0,step);
				step=it.getParent(step);
			}
			percorso.add(0,a1);
			return percorso;
		}else {
			return null;
		}
		
	}
	
	}
