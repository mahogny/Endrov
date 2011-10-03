/**
 * 
 */
package endrov.modelWindow.gl;

import javax.media.opengl.GL2;

/**
 * Selection ID as material color 
 * 
 * @author Johan Henriksson
 *
 */
public class GLMaterialSelect implements GLMaterial
	{
	private final int id;
	
	public GLMaterialSelect(int id)
		{
		this.id=id;
		}
	
	public void set(GL2 gl)
		{
		int selectColorNum=id;
		//Currently makes strong assumption of at least 24-bit colors.
		//it might bug out by signedness. need to be checked.
		//GL_BYTE could be used instead.
		byte colR=(byte)((selectColorNum    ) & 0x7F);
		byte colG=(byte)((selectColorNum>>7 ) & 0x7F);
		byte colB=(byte)((selectColorNum>>14));
//		System.out.println("out "+selectColorNum+" "+colR+" "+colG+" "+colB);
		gl.getGL2().glColor3ub(colR,colG,colB);
		
		}
	}