package dk.aau.cs.verification.VerifyTAPN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.TCTL.visitors.LTLQueryVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import net.tapaal.gui.petrinet.TAPNLens;
import dk.aau.cs.verification.NameMapping;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;

import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.Transition;

public class VerifyTAPNExporter {
    protected TimedArcPetriNet model;
	public ExportedVerifyTAPNModel export(TimedArcPetriNet model, TAPNQuery query, TAPNLens lens, NameMapping mapping, DataLayer guiModel, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery) {
		File modelFile = createTempFile(".xml");
		File queryFile;
		if (query.getCategory() == QueryCategory.CTL || query.getCategory() == QueryCategory.LTL || (lens != null && !lens.isGame() && lens.isColored())) {
			queryFile = createTempFile(".xml");
		} else {
			queryFile = createTempFile(".q");
		}
		this.model = model;

		return export(model, query, modelFile, queryFile, dataLayerQuery, lens, mapping, guiModel);
	}

	public ExportedVerifyTAPNModel export(TimedArcPetriNet model, TAPNQuery query, File modelFile, File queryFile, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery, TAPNLens lens, NameMapping mapping, DataLayer guiModel) {
		if (modelFile == null || queryFile == null)
			return null;

		try{
            this.model = model;
            outputModel(model, modelFile, mapping, guiModel);

            RenameAllPlacesVisitor placeVisitor = new RenameAllPlacesVisitor(mapping);
			query.getProperty().accept(placeVisitor, null);

            if(query.getProperty() instanceof TCTLNotNode) {
                TCTLPathToStateConverter innerQuery = (TCTLPathToStateConverter) ((TCTLNotNode) query.getProperty()).getProperty();
                if (innerQuery.getProperty() instanceof TCTLEFNode) {
                    TCTLAbstractStateProperty queryBody = ((TCTLEFNode) innerQuery.getProperty()).getProperty();
                    TCTLNotNode negatedQueryBody = new TCTLNotNode(queryBody);
                    TCTLAGNode agQuery = new TCTLAGNode(negatedQueryBody);
                    query.setProperty(agQuery);
                }
            }

			PrintStream queryStream = new PrintStream(queryFile);
            if (query == null) {
                throw new FileNotFoundException(null);
            } else if (query.getCategory() == QueryCategory.CTL || (lens != null && !lens.isGame() && lens.isColored())) {
                CTLQueryVisitor XMLVisitor = new CTLQueryVisitor();
                queryStream.append(XMLVisitor.getXMLQueryFor(query.getProperty(), dataLayerQuery == null? model.name() : dataLayerQuery.getName(), false));
            } else if (query.getCategory() == QueryCategory.LTL) {
                LTLQueryVisitor XMLVisitor = new LTLQueryVisitor();
                queryStream.append(XMLVisitor.getXMLQueryFor(query.getProperty(), null));
            } else if (lens != null && lens.isGame()) {
                CTLQueryVisitor XMLVisitor = new CTLQueryVisitor();
                queryStream.append(XMLVisitor.getXMLQueryFor(query.getProperty(), null, true));
            } else {
                queryStream.append(query.getProperty().toString());
            }
			queryStream.close();
		} catch(FileNotFoundException e) {
			System.err.append("An error occurred while exporting the model to verifytapn. Verification cancelled.");
			return null;
		}

        return new ExportedVerifyTAPNModel(modelFile.getAbsolutePath(), queryFile.getAbsolutePath());
	}
	
	protected void outputModel(TimedArcPetriNet model, File modelFile, NameMapping mapping, DataLayer guiModel) throws FileNotFoundException {
        PrintStream modelStream = new PrintStream(modelFile);

	    Collection<DataLayer> guiModels = TAPAALGUI.getCurrentTab().getGuiModels().values();

		modelStream.append("<pnml>\n");
		modelStream.append("<net id=\"" + model.name() + "\" type=\"P/T net\">\n");
		for(TimedPlace p : model.places())
			outputPlace(p, modelStream, guiModels, mapping);
		
		for(TimedTransition t : model.transitions())
			outputTransition(t,modelStream, guiModels, mapping);
		
		for(TimedInputArc inputArc : model.inputArcs())
			outputInputArc(inputArc, modelStream);
		
		for(TimedOutputArc outputArc : model.outputArcs())
			outputOutputArc(outputArc, modelStream);
		
		for(TransportArc transArc : model.transportArcs())
			outputTransportArc(transArc, modelStream);
		
		for(TimedInhibitorArc inhibArc : model.inhibitorArcs())
			outputInhibitorArc(inhibArc, modelStream);

		outputDeclarations(modelStream);

        modelStream.append("</net>\n");
		modelStream.append("</pnml>");
		modelStream.close();
	}

	protected void outputDeclarations(PrintStream modelStream){
	    return;
    }

	protected void outputPlace(TimedPlace p, PrintStream modelStream, Collection<DataLayer> guiModels, NameMapping mapping) {
        //remove the net prefix from the place name
        String placeName = mapping.map(p.name()).value2();
        Place guiPlace = null;

        for (DataLayer guiModel : guiModels ) {
            guiPlace = guiModel.getPlaceById(placeName);
            if (guiPlace != null) {
                break;
            }
        }

		modelStream.append("<place ");
		
		modelStream.append("id=\"" + p.name() + "\" ");
		modelStream.append("name=\"" + p.name() + "\" ");
		modelStream.append("invariant=\"" + p.invariant().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("initialMarking=\"" + p.numberOfTokens() + "\" ");
        modelStream.append(">\n");
        if (guiPlace == null) {
            outputPosition(modelStream, 0, 0);
        } else {
            outputPosition(modelStream, guiPlace.getPositionX(), guiPlace.getPositionY());
        }

        modelStream.append("</place>\n");
	}

	protected void outputTransition(TimedTransition t, PrintStream modelStream, Collection<DataLayer> guiModels, NameMapping mapping) {
        //remove the net prefix from the transition name
        var m = mapping.map(t.name());

        Transition guiTransition = null;
        if (m != null) {
            String transitionName = m.value2();
            for(DataLayer guiModel : guiModels){
                guiTransition = guiModel.getTransitionById(transitionName);
                if(guiTransition != null){
                    break;
                }
            }
        }

		modelStream.append("<transition ");

        modelStream.append("player=\"" + (t.isUncontrollable() ? "1" : "0") + "\" ");
        modelStream.append("id=\"" + t.name() + "\" ");
		modelStream.append("name=\"" + t.name() + "\" ");
        modelStream.append("urgent=\"" + (t.isUrgent()? "true":"false") + "\"");
        modelStream.append(">\n");

        if (guiTransition != null) {
            outputPosition(modelStream, guiTransition.getPositionX(), guiTransition.getPositionY());
        }

        modelStream.append("</transition>\n");
	}

    private void outputPosition(PrintStream modelStream, int positionX, int positionY) {
        modelStream.append("<graphics>");
        modelStream.append("<position ");
        modelStream.append("x=\"" + positionX + "\" ");
        modelStream.append("y=\"" + positionY + "\" ");
        modelStream.append("/>");
        modelStream.append("</graphics>");
    }

	protected void outputInputArc(TimedInputArc inputArc, PrintStream modelStream) {
		modelStream.append("<inputArc ");
		
		modelStream.append("inscription=\"" + inputArc.interval().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("source=\"" + inputArc.source().name() + "\" ");
		modelStream.append("target=\"" + inputArc.destination().name() + "\" ");
		if(inputArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + inputArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}

	protected void outputOutputArc(TimedOutputArc outputArc, PrintStream modelStream) {
		modelStream.append("<outputArc ");
		
		modelStream.append("inscription=\"1\" " );
		modelStream.append("source=\"" + outputArc.source().name() + "\" ");
		modelStream.append("target=\"" + outputArc.destination().name() + "\" ");
		if(outputArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + outputArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}

	protected void outputTransportArc(TransportArc transArc, PrintStream modelStream) {
		modelStream.append("<transportArc ");
		
		modelStream.append("inscription=\"" + transArc.interval().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("source=\"" + transArc.source().name() + "\" ");
		modelStream.append("transition=\"" + transArc.transition().name() + "\" ");
		modelStream.append("target=\"" + transArc.destination().name() + "\" ");
		if(transArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + transArc.getWeight().nameForSaving(false) + "\"");
		}

		modelStream.append("/>\n");
	}

	protected void outputInhibitorArc(TimedInhibitorArc inhibArc,	PrintStream modelStream) {
		modelStream.append("<inhibitorArc ");
		
		modelStream.append("inscription=\"" + inhibArc.interval().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("source=\"" + inhibArc.source().name() + "\" ");
		modelStream.append("target=\"" + inhibArc.destination().name() + "\" ");
		if(inhibArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + inhibArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}

	

	private File createTempFile(String ending) {
		File file = null;
		try {
			file = File.createTempFile("verifyta", ending);

		} catch (IOException e2) {
			e2.printStackTrace();
			return null;
		}
		return file;
	}
}
