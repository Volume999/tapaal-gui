package dk.aau.cs.io;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import java.util.Collection;

public class LoadedQueries {

    private final Collection<TAPNQuery> queries;
    private final Collection<String> messages;

    public LoadedQueries(Collection<TAPNQuery> queries, Collection<String> messages) {
        this.queries = queries;
        this.messages = messages;
    }

    public Collection<TAPNQuery> getQueries(){ return queries; }
    public Collection<String> getMessages() { return messages; }
}
