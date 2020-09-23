package Client;

import ProfileService.Profiler;

public interface IFileHandler
{
    public int runClientQueries(String inputs, Profiler clientRef);
}
