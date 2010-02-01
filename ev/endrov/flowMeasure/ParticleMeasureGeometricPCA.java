/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeasure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.decomposition.DoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;

import endrov.imageset.EvStack;
import endrov.util.EvMathUtil;

/**
 * Measure: Geometric PCA
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureGeometricPCA implements ParticleMeasure.MeasurePropertyType 
	{
	private static String propertyName="gpca";

	/**
	 * Variables to calculate covariance the fast beer
	 */
	private static class Cov
		{
		public double sumx=0;
		public double sumy=0;
		public double sumz=0;
		public double sumxx=0;
		public double sumxy=0;
		public double sumxz=0;
		public double sumyy=0;
		public double sumyz=0;
		public double sumzz=0;
		public int count=0;
		}
	
	public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		//TODO should thickness be taken into account? world or pixel coordinates?
		
		HashMap<Integer,Cov> sumX=new HashMap<Integer, Cov>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		for(int az=0;az<stackValue.getDepth();az++)
			{
			//double[] arrValue=stackValue.getInt(az).getPixels().convertToDouble(true).getArrayDouble();
			int[] arrID=stackMask.getInt(az).getPixels().convertToInt(true).getArrayInt();
			
			int w=stackValue.getWidth();
			int h=stackValue.getHeight();

			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					int index=ay*w+ax;

					//double v=arrValue[index];
					int id=arrID[index];
		
					if(id!=0)
						{
						Cov lastSum=sumX.get(id);
						if(lastSum==null)
							sumX.put(id,lastSum=new Cov());

						lastSum.sumx+=ax;
						lastSum.sumy+=ay;
						lastSum.sumz+=az;
						lastSum.sumxx+=ax*ax;
						lastSum.sumxy+=ax*ay;
						lastSum.sumxz+=ax*az;
						lastSum.sumyy+=ay*ay;
						lastSum.sumyz+=ay*az;
						lastSum.sumzz+=az*az;
						lastSum.count++;
						}
					}
			
			}
		
		//Write into particles
		for(int id:sumX.keySet())
			{
			HashMap<String, Object> p=info.getCreate(id);

			//Calculate covariance
			Cov cov=sumX.get(id);
			double cxx=EvMathUtil.biasedCovariance(cov.sumx, cov.sumx, cov.sumxx, cov.count);
			double cxy=EvMathUtil.biasedCovariance(cov.sumx, cov.sumy, cov.sumxy, cov.count);
			double cxz=EvMathUtil.biasedCovariance(cov.sumx, cov.sumz, cov.sumxz, cov.count);
			double cyy=EvMathUtil.biasedCovariance(cov.sumy, cov.sumy, cov.sumyy, cov.count);
			double cyz=EvMathUtil.biasedCovariance(cov.sumy, cov.sumz, cov.sumyz, cov.count);
			double czz=EvMathUtil.biasedCovariance(cov.sumz, cov.sumz, cov.sumzz, cov.count);

			//Do SVD for vectors and eigenvalues
			DenseDoubleMatrix2D m=new DenseDoubleMatrix2D(
					new double[][]{
								{cxx,cxy,cxz},
								{cxy,cyy,cyz},
								{cxz,cyz,czz}});
			DoubleEigenvalueDecomposition de=new DoubleEigenvalueDecomposition(m);

			DoubleMatrix1D eigVal=de.getRealEigenvalues();
			for(int i=0;i<3;i++)
				p.put(propertyName+"L"+(i+1), eigVal.get(0));

			String[] xyz=new String[]{"x","y","z"};
			DoubleMatrix2D eigvec=de.getV();
			for(int i=0;i<3;i++)
				for(int j=0;j<3;j++)
					p.put(propertyName+"V"+(i+1)+xyz[j], eigvec.get(j, i));
			
			//Or put a vector instead?
			}
		}

	public String getDesc()
		{
		return "Principal component analysis, takes only geometry into consideration";
		}

	public Set<String> getColumns()
		{
		HashSet<String> set=new HashSet<String>();
		for(int i=0;i<3;i++)
			set.add(propertyName+"L"+(i+1));
		String[] xyz=new String[]{"x","y","z"};
		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++)
				set.add(propertyName+"V"+(i+1)+xyz[j]);
		return set;
		}

	
	
	
	}
