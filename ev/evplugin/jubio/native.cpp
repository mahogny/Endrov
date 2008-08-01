#include <jni.h>
#include <stdlib.h>
#include "jubio.hh"


////////////////////////////////////////////////////////////////////////////////
// Check if an image is totally black
bool isTotallyBlack(Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> &slice)
	{
	int x=slice.width()/2;
	int y=slice.height()/2;
	return (slice(x+0,y+0,0)==0   && slice(x+0,y+1,0)==0   && slice(x+0,y+2,0)==0   && slice(x+0,y+3,0)==0  ) ||
		(slice(x+0,y+0,0)==255 && slice(x+0,y+1,0)==255 && slice(x+0,y+2,0)==255 && slice(x+0,y+3,0)==255);
	}


extern "C"
{


JNIEXPORT jint JNICALL Java_evplugin_jubio_Jubio__1load
  (JNIEnv *e, jclass c, jstring filename, jint slice)
	{
	jboolean iscopy;
	const char *mfile = e->GetStringUTFChars(filename,&iscopy);
	Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *jImg=new Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy>();

	try
		{
		if(slice==-1)
			jImg->load(mfile);			
		else
			jImg->load(mfile,slice);
		}
	catch(Jubio::JubioIOError& e)
		{
		system("echo file could not be loaded");
		delete jImg;
		jImg=NULL;
		}
	e->ReleaseStringUTFChars(filename, mfile);



	return (int)jImg;
	}

JNIEXPORT void JNICALL Java_evplugin_jubio_Jubio__1unload
  (JNIEnv *e, jclass c, jint addr)
	{
	Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *img = (Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *)addr;
	delete img;
	}

JNIEXPORT jint JNICALL Java_evplugin_jubio_Jubio__1getWidth
  (JNIEnv *e, jclass c, jint addr)
	{
	Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *img = (Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *)addr;
	return img->width();
	}

JNIEXPORT jint JNICALL Java_evplugin_jubio_Jubio__1getHeight
  (JNIEnv *e, jclass c, jint addr)
	{
	Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *img = (Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *)addr;
	return img->height();
	}

JNIEXPORT jint JNICALL Java_evplugin_jubio_Jubio__1getDepth
  (JNIEnv *e, jclass c, jint addr)
	{
	Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *img = (Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *)addr;
	return img->depth();
	}


JNIEXPORT jbyteArray JNICALL Java_evplugin_jubio_Jubio__1getDataArray
  (JNIEnv *e, jclass c, jint addr, jint slice)
	{
	jbyteArray jb;

	Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *img = (Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *)addr;
	int w=img->width();
	int h=img->height();

	jb=e->NewByteArray(w*h);
	e->SetByteArrayRegion(jb, 0, w*h, (jbyte *)&(*img)(0,0,slice));

	return jb;
	}



JNIEXPORT jint JNICALL Java_evplugin_jubio_Jubio__1calcMax
  (JNIEnv *e, jclass c, jint addr)
	{
	Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *jImg = (Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *)addr;

	Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *maxImg=
		new Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy>(jImg->getSliceCpy(0));

	//Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> *maxImg = 
	//jImg.getSliceCpy(0);
	
	for(int ay=0;ay<jImg->height();ay++)
		for(int ax=0;ax<jImg->width();ax++)
			(*maxImg)(ax,ay,0)=0;
	
	//For each slice
	for(int j=0;j<jImg->depth();j++)
		{
		Jubio::Image<Jubio::GrayData<Jubio::ubyte>,Jubio::PreloadPolicy> slice=jImg->getSliceCpy(j);

		//Don't store totally black or totally white frames
		if(!isTotallyBlack(slice))
			{
			//Update maximum channel
			for(int ay=0;ay<jImg->height();ay++)
				for(int ax=0;ax<jImg->width();ax++)
					if((*maxImg)(ax,ay,0)<slice(ax,ay,0))
						(*maxImg)(ax,ay,0)=slice(ax,ay,0);
			}
		}
	
	return (int)maxImg;
	}

}


