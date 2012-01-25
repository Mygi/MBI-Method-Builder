package nig.mf.plugin.pssd.method;

import java.util.*;


/**
 * This class is not currently used in the PSSD system so we can't work out the Collection types
 * 
 * @author jason
 *
 */
public class StackFrame {
	
	private StackFrame 	_pf;
	private Method     	_method;
	private int   	  	_step;
	private Set    	  	_completed;
	
	private Vector _subFrames;
	
	/**
	 * Constructor.
	 * 
	 * @param method
	 */
	public StackFrame(StackFrame parent,Method method,int step) {
		_pf = parent;
		_method = method;
		_step = step;
		_completed = null;
		_subFrames = null;
	}
	
	/**
	 * The parent frame, or null.
	 * 
	 * @return
	 */
	public StackFrame parent() {
		return _pf;
	}
	
	/**
	 * Method being executed by this frame.
	 * 
	 * @return
	 */
	public Method method() {
		return _method;
	}
	
	/**
	 * Step being executed by this frame. This is the first
	 * un-completed step.
	 * 
	 * @return
	 */
	public int step() {
		return _step;
	}
	
	/**
	 * Returns the specified frame, or null if not found.
	 * 
	 * @param method
	 * @param step
	 * @return
	 */
	public StackFrame frame(int step) {
		if ( _subFrames == null ) { 
			return null;
		}
		
		for ( int i=0; i < _subFrames.size(); i++ ) {
			StackFrame sf = (StackFrame)_subFrames.get(i);
			if ( sf.step() == step ) {
				return sf;
			}
		}
		
		return null;
	}
	
	/**
	 * Starts execution of and adds the specified sub-frame to this frame.
	 * 
	 * @param frame
	 */
	public boolean start(StackFrame frame) {
		
		if ( frame(frame.step()) != null ) {
			return false;
		}
		
		if ( _subFrames == null ) {
			_subFrames = new Vector();
		}
		
		_subFrames.add(frame);
		
		// Sort into ascending order.
		Collections.sort(_subFrames,new Comparator() {

			public int compare(Object a, Object b) {
				StackFrame sfa = (StackFrame)a;
				StackFrame sfb = (StackFrame)b;
				return sfb.step() - sfa.step();
			}
			
		});
		
		// Connect that frame to this frame.
		frame._pf = this;
		
		return true;
	}
	
	/**
	 * Lowest frame and the first frame of execution in this stack.
	 * 
	 * @return The first executing frame which may be self.
	 */
	public StackFrame first() {
		if ( _subFrames == null || _subFrames.size() == 0 ) {
			return this;
		}
		
		StackFrame sf = (StackFrame)_subFrames.get(0);
		return sf.first();
	}
	
	/**
	 * Returns the current executing sub-frames, if any.
	 * 
	 * @return
	 */
	public List frames() {
		return _subFrames;
	}
	
	/**
	 * This stack frame has completed execution.
	 * 
	 */
	public void complete() {
		StackFrame pf = parent();
		if ( pf == null ) {
			return;
		}
		
		pf.completed(this);
	}
	
	/**
	 * Set of completed steps for this frame.
	 * 
	 * @return An array of Integer objects (step numbers) or null if none completed.
	 */
	public Set completedSteps() {
		return _completed;
	}
	
	/**
	 * Removes the specified sub-frame.
	 * 
	 * @param frame
	 */
	private boolean completed(StackFrame frame) {
		if ( _subFrames == null ) {
			return false;
		}
		
		StackFrame sf = frame(frame.step());
		if ( sf == null ) {
			return false;
		}
		
		_subFrames.remove(sf);
		
		if ( _completed == null ) {
			_completed = new TreeSet();
		}
		
		_completed.add(new Integer(frame.step()));
		return true;
	}
	
}
