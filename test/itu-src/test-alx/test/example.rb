require 'java'
require '/Users/alex/Desktop/test/lib/ebwsclient.jar'
require '/Users/alex/Desktop/test/lib/jetty-util-8.0.0.M0.jar'

include_class Java::dk.itu.infobus.ws.EventBus
include_class Java::dk.itu.infobus.ws.PatternBuilder
include_class Java::dk.itu.infobus.ws.PatternOperator
include_class Java::dk.itu.infobus.ws.GeneratorAdapter
include_class Java::dk.itu.infobus.ws.Listener

#a sample generator
class SampleGenerator < GeneratorAdapter
  
  def init
    super("sample-alx.generator", "foo", "bar")
  end
  
  def on_started  
    thread = Thread.new do
      
      def run
        foos = [1, 2, 3]
        bars = [5, 10, 15]
        
        # the EventBuilder class is a utility class that wraps a Map
				# its use is to create quickly maps using chaining
        eb = EventBuilder.new
        
        #infinite loop to generate the events
        i = 0
        while (true) do
          
          #discard the current event (map)
          eb.reset
          
          Thread.sleep(500)
          publish(eb.put("foo", foos[i]).put("bar", bars[i]).get_event)
          
          i = (i+1) % foos.length
        end
      end
      
      #end anonymous class definition
    end
    
    thread.start
  end
  
  
end

# create a sample listener
def create_listener
  
  pb = PatternBuilder.new.add_match_all("foo")
  #overridding the listener
  listener = Listener.new(pb.get_pattern) do  
    
    # is used to define some initialization. It is called when the 
		# listener has been registered on the Genie Hub server
    def on_started
      put "listener started"
    end
    
    # is called when an event matching the pattern has been 
		# received
    def on_message(msg)
      Util.trace_event(msg)
    end
    
  end
  
  listener
  
end

eb = EventBus.new("tiger.itu.dk", 8004)

# the status listener for the event bus


#starting the event bus 
#eb.set_status_listener(status)
eb.start
eb.add_listener(create_listener)
eb.add_generator(SampleGenerator.new)
