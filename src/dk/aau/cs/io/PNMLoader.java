package dk.aau.cs.io;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.Weight;
import dk.aau.cs.util.FormatException;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Zoomer;
import pipe.gui.graphicElements.AnnotationNote;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.Note;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import pipe.gui.handler.AnimationHandler;
import pipe.gui.handler.AnnotationNoteHandler;
import pipe.gui.handler.ArcHandler;
import pipe.gui.handler.LabelHandler;
import pipe.gui.handler.PlaceHandler;
import pipe.gui.handler.TAPNTransitionHandler;
import pipe.gui.handler.TimedArcHandler;
import pipe.gui.handler.TransitionHandler;
import pipe.gui.handler.TransportArcHandler;

public class PNMLoader {
	
	enum GraphicsType { Position, Offset }

	private DrawingSurfaceImpl drawingSurface;
	private NameGenerator nameGenerator = new NameGenerator();
	private IdResolver idResolver = new IdResolver();
	
	//If the net is too big, do not make the graphics
	private int netSize = 0;
	private int maxNetSize = 2000;
	
	public PNMLoader(DrawingSurfaceImpl drawingSurface) {
		this.drawingSurface = drawingSurface;
	}
	
	public LoadedModel load(File file) throws FormatException{
		try{
			return load(new FileInputStream(file));
		} catch (FileNotFoundException e){
			return null;
		}
	}
	
	public LoadedModel load(InputStream file) throws FormatException{
		Document doc = loadDocument(file);
		
		return parse(doc);
	}

	private Document loadDocument(InputStream file) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(file);
		} catch (ParserConfigurationException e) {
			return null;
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	private LoadedModel parse(Document doc) throws FormatException {
		idResolver.clear();
		
		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
		
		//We assume there is only one net per file (this is what we call a TAPN Network) 
		Node pnmlElement = doc.getElementsByTagName("pnml").item(0);
		Node netNode = getFirstDirectChild(pnmlElement, "net");
		
		String name = getTAPNName(netNode);
		
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);
		network.add(tapn);
		nameGenerator.add(tapn);

		//We assume there is only one page pr. file (this is what we call a net) 
		Template template = new Template(tapn, new DataLayer(), new Zoomer());
	
		parseTimedArcPetriNet(netNode, tapn, template);
		
		network.setPaintNet(isNetDrawable());
		
		return new LoadedModel(network, Arrays.asList(template), new ArrayList<TAPNQuery>());
	}

	private String getTAPNName(Node netNode) {
		if(netNode == null || !(netNode instanceof Element)){
			return "Default";
		}
		String result = "Default";

		Node name =  getFirstDirectChild(netNode, "name");
		if(name != null){
			result = getFirstDirectChild(name, "text").getTextContent();
		}
		
		result = result.trim();
		result = result.replace(".", "_");
		result = result.replace(" ", "_");
		
		//TODO Fix the name if not allowed
		return result == null ? "Default" : result;
	}

	private void parseTimedArcPetriNet(Node netNode, TimedArcPetriNet tapn, Template template) throws FormatException {
		//We assume there is only one page pr. file (this is what we call a net) 
		Node node = getFirstDirectChild(netNode, "page").getFirstChild();
		Node first = node;
		
		//Calculate netsize
		while(node != null){
			netSize += 1;
			node = node.getNextSibling();
		}
		
		template.guiModel().setDrawable(isNetDrawable());
		
		node = first;
		//We parse the places and transitions first
		while(node != null){
			String tag = node.getNodeName();
			if(tag.equals("place")){
				parsePlace(node, tapn, template);
			} else if(tag.equals("transition")){
				parseTransition(node, tapn, template);
			}
			node = node.getNextSibling();
		}
		
		//We parse the transitions last, as we need the places and transitions it refers to
		node = first;
		while(node != null){
			String tag = node.getNodeName();
			if(tag.equals("arc")){
				parseArc(node, tapn, template);
			} 
			node = node.getNextSibling();
		}
	}

	private void parsePlace(Node node, TimedArcPetriNet tapn, Template template) {
		if(node == null || !(node instanceof Element)){
			return;
		}
		
		Name name = parseName(getFirstDirectChild(node, "name"));
		Point position = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Position);
		String id = ((Element) node).getAttribute("id");
		InitialMarking marking = parseMarking(getFirstDirectChild(node, "initialMarking")); 
		
		TimedPlace place = new LocalTimedPlace(name.name);
		tapn.add(place);		
		
		if(isNetDrawable()){
			TimedPlaceComponent placeComponent = new TimedPlaceComponent(position.getX(), position.getY(), id, name.name, name.point.getX(), name.point.getY(),
				marking.marking, marking.point.x, marking.point.y, 0);
			placeComponent.setUnderlyingPlace(place);
			template.guiModel().addPetriNetObject(placeComponent);
			addListeners(placeComponent, template);
		}
		
		idResolver.add(tapn.name(), id, name.name);
		
		for (int i = 0; i < marking.marking; i++) {
			tapn.parentNetwork().marking().add(new TimedToken(place));
		}
	}
	
	private InitialMarking parseMarking(Node node) {
		if(node == null || !(node instanceof Element)){
			return new InitialMarking();
		}
		
		Element element = (Element) node;
		Point offset = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Offset);
		
		int marking = Integer.parseInt(getFirstDirectChild(node, "text").getTextContent());
		
		return new InitialMarking(marking, offset);
	}

	private void parseTransition(Node node, TimedArcPetriNet tapn, Template template) {
		if(node == null || !(node instanceof Element)){
			return;
		}
		
		Point position = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Position);
		Name name = parseName(getFirstDirectChild(node, "name"));
		String id = ((Element) node).getAttribute("id");
		
		TimedTransition transition = new TimedTransition(name.name);
		tapn.add(transition);
		
		if(isNetDrawable()){
			TimedTransitionComponent transitionComponent = 
				new TimedTransitionComponent(position.getX(), position.getY(), id, name.name, name.point.getX(), name.point.getY(), 
						true, false, 0, 0);
			transitionComponent.setUnderlyingTransition(transition);
			template.guiModel().addPetriNetObject(transitionComponent);
			addListeners(transitionComponent, template);
		}
		idResolver.add(tapn.name(), id, name.name);
	}
	
	private void parseArc(Node node, TimedArcPetriNet tapn, Template template) throws FormatException {
		if(node == null || !(node instanceof Element)){
			return;
		}
		
		Element element = (Element) node;
		
		String id = element.getAttribute("id");
		String sourceId = element.getAttribute("source");
		String targetId = element.getAttribute("target");
		
		String sourceName = idResolver.get(template.model().name(), sourceId);
		String targetName = idResolver.get(template.model().name(), targetId);
		
		TimedPlace sourcePlace = template.model().getPlaceByName(sourceName);
		TimedPlace targetPlace = template.model().getPlaceByName(targetName);
		
		TimedTransition sourceTransition = template.model().getTransitionByName(sourceName);
		TimedTransition targetTransition = template.model().getTransitionByName(targetName);
		
		PlaceTransitionObject source = template.guiModel().getPlaceTransitionObject(sourceName);
		PlaceTransitionObject target = template.guiModel().getPlaceTransitionObject(targetName);
		
		int _startx = 0, _starty = 0, _endx = 0, _endy = 0;
		
		if(isNetDrawable()){
			// add the insets and offset
			_startx = source.getX() + source.centreOffsetLeft();
			_starty = source.getY() + source.centreOffsetTop();

			_endx = target.getX() + target.centreOffsetLeft();
			_endy = target.getY() + target.centreOffsetTop();
		}
		
		//TODO weights!!
		//TODO arcpath!! 
		
		Arc arc;
		
		if(sourcePlace != null && targetTransition != null) {
			arc = parseInputArc(id, sourcePlace, targetTransition, source, target, _startx, _starty, _endx, _endy, tapn, template);
		} else if(sourceTransition != null && targetPlace != null) {
			arc = parseOutputArc(id,  sourceTransition, targetPlace, source, target, _startx, _starty, _endx, _endy, tapn, template);
		} else {
			throw new FormatException("Arcs much be between places and transitions");
		}
		
		//TODO Parse ArcPath
	}

	private Name parseName(Node node){
		if(node == null || !(node instanceof Element)){
			return new Name();
		}
		
		Point offset = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Offset);
		
		String name = getFirstDirectChild(node, "text").getTextContent();
		
		name = name.trim();
		name = name.replace(".", "_");
		name = name.replace(" ", "_");
		
		return new Name(name, offset);
	}
	
	private Point parseGraphics(Node node, GraphicsType type){
		if(node == null || !(node instanceof Element)){
			if(type == GraphicsType.Offset)
				return new Point(0, -10);
			else 
				return new Point(100, 100);
		}
		
		Element offset = (Element)getFirstDirectChild(node, type == GraphicsType.Offset ? "offset" : "position");
		
		String x = offset.getAttribute("x");
		String y = offset.getAttribute("y");
		
		int xd = Integer.parseInt(x);
		int yd = Integer.parseInt(y);
		
		return new Point(xd, yd);
	}

	private class Name{
		String name;
		Point point;
		
		public Name() {
			this("", new Point());
		}
		
		public Name(String name, Point p) {
			this.name = name;
			this.point = p;
		}
		
		@Override
		public String toString() {
			return name + ";" + point;
		}
	}
	
	private class InitialMarking{
		int marking;
		Point point;
		
		public InitialMarking() {
			this(0, new Point());
		}
			
		
		public InitialMarking(int marking, Point p) {
			this.marking = marking;
			this.point = p;
		}
		
		@Override
		public String toString() {
			return marking + ";" + point;
		}
	}
	
	private TimedInputArcComponent parseInputArc(String arcId, TimedPlace place, TimedTransition transition, PlaceTransitionObject source,
			PlaceTransitionObject target, int _startx, int _starty, int _endx,
			int _endy, TimedArcPetriNet tapn, Template template) throws FormatException {

		TimedInputArc inputArc = new TimedInputArc(place, transition, TimeInterval.ZERO_INF); //TODO Weight
		
		if(template.model().hasArcFromPlaceToTransition(inputArc.source(), inputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}
		
		TimedInputArcComponent arc = null;
		
		if(isNetDrawable()){
			arc = new TimedInputArcComponent(new TimedOutputArcComponent(
				_startx, _starty, _endx, _endy, source, target, 1, arcId,
				false));
			arc.setUnderlyingArc(inputArc);

			template.guiModel().addPetriNetObject(arc);
			addListeners(arc, template);

			source.addConnectFrom(arc);
			target.addConnectTo(arc);
		}
		
		template.model().add(inputArc);
		
		return arc;

		
	}
	
	private Arc parseOutputArc(String arcId, TimedTransition transition, TimedPlace place, PlaceTransitionObject source,
			PlaceTransitionObject target, int _startx, int _starty, int _endx,
			int _endy, TimedArcPetriNet tapn, Template template) throws FormatException {
		
		TimedOutputArc outputArc = new TimedOutputArc(transition, place); //Weight	
		
		if(template.model().hasArcFromTransitionToPlace(outputArc.source(),outputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}
		
		TimedOutputArcComponent arc = null;
		
		if(isNetDrawable()){
			arc = new TimedOutputArcComponent(_startx, _starty, _endx, _endy, 
				source, target, 1,	arcId, false); //TODO weight
			arc.setUnderlyingArc(outputArc);

			template.guiModel().addPetriNetObject(arc);
			addListeners(arc, template);

			source.addConnectFrom(arc);
			target.addConnectTo(arc);
		}
		
		template.model().add(outputArc);
		return arc;
	}
	
	private boolean isNetDrawable(){
		return netSize <= maxNetSize;
	}
	
	Node getFirstDirectChild(Node parent, String tagName){
		if(parent == null)
			System.err.println("NNNOOOO!!!");
		NodeList children = parent.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			if(children.item(i).getNodeName().equals(tagName)){
				return children.item(i);
			}
		}
		return null;
	}
	
	private void addListeners(PetriNetObject newObject, Template template) {
		if (newObject != null) {
			if (newObject.getMouseListeners().length == 0) {
				if (newObject instanceof Place) {
					// XXX - kyrke
					if (newObject instanceof TimedPlaceComponent) {

						LabelHandler labelHandler = new LabelHandler(((Place) newObject).getNameLabel(), (Place) newObject);
						((Place) newObject).getNameLabel().addMouseListener(labelHandler);
						((Place) newObject).getNameLabel().addMouseMotionListener(labelHandler);
						((Place) newObject).getNameLabel().addMouseWheelListener(labelHandler);

						PlaceHandler placeHandler = new PlaceHandler(drawingSurface, (Place) newObject, template.guiModel(), template.model());
						newObject.addMouseListener(placeHandler);
						newObject.addMouseWheelListener(placeHandler);
						newObject.addMouseMotionListener(placeHandler);
					} else {

						LabelHandler labelHandler = new LabelHandler(((Place) newObject).getNameLabel(), (Place) newObject);
						((Place) newObject).getNameLabel().addMouseListener(labelHandler);
						((Place) newObject).getNameLabel().addMouseMotionListener(labelHandler);
						//((Place) newObject).getNameLabel().addMouseWheelListener(labelHandler);

						PlaceHandler placeHandler = new PlaceHandler(drawingSurface, (Place) newObject);
						newObject.addMouseListener(placeHandler);
						//newObject.addMouseWheelListener(placeHandler);
						newObject.addMouseMotionListener(placeHandler);

					}
				} else if (newObject instanceof Transition) {
					TransitionHandler transitionHandler;
					if (newObject instanceof TimedTransitionComponent) {
						transitionHandler = new TAPNTransitionHandler(drawingSurface, (Transition) newObject, template.guiModel(), template.model());
					} else {
						transitionHandler = new TransitionHandler(drawingSurface, (Transition) newObject);
					}

					LabelHandler labelHandler = new LabelHandler(((Transition) newObject).getNameLabel(), (Transition) newObject);
					((Transition) newObject).getNameLabel().addMouseListener(labelHandler);
					((Transition) newObject).getNameLabel().addMouseMotionListener(labelHandler);
					((Transition) newObject).getNameLabel().addMouseWheelListener(labelHandler);

					newObject.addMouseListener(transitionHandler);
					newObject.addMouseMotionListener(transitionHandler);
					newObject.addMouseWheelListener(transitionHandler);

					newObject.addMouseListener(new AnimationHandler());

				} else if (newObject instanceof Arc) {
					/* CB - Joakim Byg add timed arcs */
					if (newObject instanceof TimedInputArcComponent) {
						if (newObject instanceof TimedTransportArcComponent) {
							TransportArcHandler transportArcHandler = new TransportArcHandler(drawingSurface, (Arc) newObject);
							newObject.addMouseListener(transportArcHandler);
							//newObject.addMouseWheelListener(transportArcHandler);
							newObject.addMouseMotionListener(transportArcHandler);
						} else {
							TimedArcHandler timedArcHandler = new TimedArcHandler(drawingSurface, (Arc) newObject);
							newObject.addMouseListener(timedArcHandler);
							//newObject.addMouseWheelListener(timedArcHandler);
							newObject.addMouseMotionListener(timedArcHandler);
						}
					} else {
						/* EOC */
						ArcHandler arcHandler = new ArcHandler(drawingSurface,(Arc) newObject);
						newObject.addMouseListener(arcHandler);
						//newObject.addMouseWheelListener(arcHandler);
						newObject.addMouseMotionListener(arcHandler);
					}
				} else if (newObject instanceof AnnotationNote) {
					AnnotationNoteHandler noteHandler = new AnnotationNoteHandler(drawingSurface, (AnnotationNote) newObject);
					newObject.addMouseListener(noteHandler);
					newObject.addMouseMotionListener(noteHandler);
					((Note) newObject).getNote().addMouseListener(noteHandler);
					((Note) newObject).getNote().addMouseMotionListener(noteHandler);
				}
				
				newObject.zoomUpdate(drawingSurface.getZoom());
				
			}
			newObject.setGuiModel(template.guiModel());
		}
	}
}
