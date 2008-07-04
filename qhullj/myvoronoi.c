//Taken from octave and modified. GPL infected.

#include <jni.h>

#include "qhull_QHull.h"

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <qhull_a.h>
#ifdef NEED_QHULL_VERSION
char qh_version[] = "qhullj";
#endif

#ifndef false
#define false 0
#endif

#ifndef true
#define true 1
#endif

JNIEXPORT jboolean JNICALL Java_qhull_QHull_voronoi_1
  (JNIEnv *env, jclass jc, jobject jresult, jobjectArray points)
  {
  jclass vresultsCls=(*env)->GetObjectClass(env,jresult);
  jint np=(*env)->GetArrayLength(env,points);
  	
  	
  	
  	
  //Convert points to pt_array, suitable for input
  double pt_array[np*3];
  int dim=3;
  for(int i=0;i<np;i++)
  	{
	jobject pv=(*env)->GetObjectArrayElement(env,points,i);
	jclass vecclass=(*env)->GetObjectClass(env,pv);
  	jfieldID fieldX=(*env)->GetFieldID(env, vecclass, "x", "D");
  	jfieldID fieldY=(*env)->GetFieldID(env, vecclass, "y", "D");
  	jfieldID fieldZ=(*env)->GetFieldID(env, vecclass, "z", "D");
  	pt_array[i*3+0]=(*env)->GetDoubleField(env,pv,fieldX);
  	pt_array[i*3+1]=(*env)->GetDoubleField(env,pv,fieldY);
  	pt_array[i*3+2]=(*env)->GetDoubleField(env,pv,fieldZ);
  	}
  
  char options[]="";  
  char flags[250];
  sprintf (flags, "qhull v Fv T0 %s", options);
  
////////////////////////////////


  int ismalloc = false;

  FILE *outfile = 0;
  FILE *errfile = stderr;

  if(!qh_new_qhull (dim, np, pt_array, ismalloc, flags, outfile, errfile)) 
    {
    facetT *facet;
    vertexT *vertex;


    int n = 1, k = 0, m = 0, fidx = 0, j = 0;//, r = 0;
    
    int ni[np];
	for (int i = 0; i < np; i++) ni[i] = 0;
	
    qh_setvoronoi_all ();
    int infinity_seen = false;
    facetT *neighbor, **neighborp;
    coordT *voronoi_vertex;

    FORALLfacets 
		{
		facet->seen = false;
		}

    FORALLvertices 
	  {
	  if (qh hull_dim == 3)
	    qh_order_vertexneighbors (vertex);
	  infinity_seen = false;

	  FOREACHneighbor_ (vertex)
	    {
	    if (! neighbor->upperdelaunay)
		  {
		  if (! neighbor->seen)
		    {
		    n++;
		    neighbor->seen = true;
		    }
		  ni[k]++;
		  }
	   else if (! infinity_seen)
	  	{
		  infinity_seen = true;
		  ni[k]++;
		}
	    }
	  k++;
	}




    double v[n][dim];
//    Matrix v (n, dim);
    for (int d = 0; d < dim; d++)
  	  v[0][d] = 99999999; //infinity


	int AtInf[np]; //need to malloc

      for (int i = 0; i < np; i++) 
		AtInf[i] = 0;
	
	jobject *faceArray=NULL;
	int faceArraySize=0;
//      octave_value_list F;
      k = 0;
      int newi = 0;

      FORALLfacets 
	{
	  facet->seen = false;
	}

      FORALLvertices
	{
	  if (qh hull_dim == 3)
	    qh_order_vertexneighbors(vertex);
	  infinity_seen = false;
	  
	  
	  jintArray facet_list=(*env)->NewIntArray(env, ni[k++]);
		  
	  
	  
	  m = 0;

	  FOREACHneighbor_(vertex)
	    {
	      if (neighbor->upperdelaunay)
		{
		  if (! infinity_seen)
		    {
		      infinity_seen = true;
		      jint one=1;
              (*env)->SetIntArrayRegion(env, facet_list, m++, 1, (jint *)(&one));
//              m++;              
//		      facet_list(m++) = 1;
		      AtInf[j] = true;
		    }
		} 
	      else 
		{
		  if(!neighbor->seen)
		    {
		      voronoi_vertex = neighbor->center;
		      fidx = neighbor->id;
		      newi++;
		      for (int d = 0; d < dim; d++)
			{
			  v[newi][d] = *(voronoi_vertex++);
			}
		      neighbor->seen = true;
		      neighbor->visitid = newi;
		    }
          
          jint pos=neighbor->visitid + 1;
          (*env)->SetIntArrayRegion(env, facet_list, m++, 1, (jint *)(&pos));
//		  facet_list(m++) = neighbor->visitid + 1;
		}
	    }
	    
	    
	    faceArraySize++;
	    faceArray=realloc(faceArray,sizeof(jobject*)*faceArraySize);
	    faceArray[faceArraySize-1]=facet_list;
	    
//	  F(r++) = facet_list;
	  
	  j++;
	}



//	*outv=v;
//	*outC=C;
//	*outAtInf=AtInf;

	//Convert C faces *[I to [[I
    jclass intArrCls = (*env)->FindClass(env,"[I") ;
  	jobjectArray faceJavaArray=(*env)->NewObjectArray(env, faceArraySize, intArrCls, NULL);
  	for(int i=0;i<faceArraySize;i++)
		(*env)->SetObjectArrayElement(env, faceJavaArray, faceArraySize, faceArray[i]);
	free(faceArray);
	
	//Convert *vertex to [Vector3d
	
	//TODO


//	jobject pv=(*env)->GetObjectArrayElement(env,points,i);
//  	pt_array[i*3+0]=(*env)->GetDoubleField(env,pv,fieldX);
	
	//Set result field faces
  	jfieldID fieldFaces=(*env)->GetFieldID(env, vresultsCls, "faces", "[[I");
	(*env)->SetObjectField(env, jresult, fieldFaces, faceJavaArray); 

    //Set result field vector
  	jfieldID fieldVectors=(*env)->GetFieldID(env, vresultsCls, "vectors", "[javax/vecmath/Vector3d");


    // free long memory
      qh_freeqhull (! qh_ALL);

      // free short memory and memory allocator
      int curlong, totlong;
      qh_memfreeshort (&curlong, &totlong);

      if (curlong || totlong)
		printf ("voronoi: did not free %d bytes of long memory (%d pieces)", totlong, curlong);
	return JNI_TRUE;
    }
  else
  	return JNI_FALSE;


////////////////  




/*

	//pack faces into one single vector for simplicity  	
  	int jfacesLen=5;
  	jint packFaces[jfacesLen];
  	jintArray jfaces=env->NewIntArray(env, jfacesLen);
    env->SetIntArrayRegion(env, jfaces, 0, jfacesLen, packFaces);
  	
  	
  	
  */	
  	
  	
  	
  }



