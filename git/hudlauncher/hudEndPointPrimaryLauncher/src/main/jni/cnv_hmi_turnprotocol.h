#ifndef _CNV_HMI_TURNPROTOCOL_H_
#define _CNV_HMI_TURNPROTOCOL_H_

#define GET_BIT_VALUE(Value,nBit) ((Value>>nBit) & 1)	//ȡĳλBit�Ƿ�����
#define SET_BIT_VALUE(Value,nBit) (Value = (Value | (1<<nBit)))	//��ĳλBit
#define CLEAN_BIT_VALUE(Value,nBit) (Value = (Value & (~(1<<nBit))))	//��ĳλBit

typedef	enum{
	GuidanceInfoType_Turn = 0,	//ת��
	GuidanceInfoType_HightWay,	//����
	GuidanceInfoType_Lane,	    //����
	GuidanceInfoType_Camera,	//������
	GuidanceInfoType_DetailJV,	//JV��ϸ��Ϣ
	GuidanceInfoType_CatchJV,	//JV��ͼ
	GuidanceInfoType_CatchMap,	//��ͼ��ͼ

	GuidanceInfoType_Max = 32,	//���ֵ ��Ҫ���ڴ�֮ǰ
}GuidanceInfoType;

/*=============================================================
�ڴ�ӳ��
================================================================*/
typedef struct tag_GuidanceInfoMemMap
{
	long lVersion;				//��ǰЭ��汾
	long lMapSize;				//ӳ���С
	unsigned char uMapName[32];	//ӳ������
}GuidanceInfoMemMap;

typedef struct tag_GuidanceInfoDataDir
{
	long lOffset;				//��ӳ���ڴ��׵�ַ��ƫ��
	long lMapSize;				//��С
	long lIsDataValid;			//�����Ƿ���Ч һ�����ڵ����ִ��е������ݻ���޵��е����
}GuidanceInfoDataDir;

typedef struct tag_MemMapDataHeader
{
	long lGuidanceDataFlag;							//ĳλ��1��ʾ����Ϣ�仯,������λ��ʾʲô��GuidanceInfoType
	GuidanceInfoDataDir lGuidanceInfoDataDir[GuidanceInfoType_Max];			//����Ŀ¼(��Ϊ������)
}MemMapDataHeader;


typedef struct tag_GuidanceHightWayInfo
{
	unsigned short uSAName[32];	//����������
	long lSADis;				//����������
	unsigned short uJCName[32];	//���ӿ�����
	long lJCDis;				//���ӿھ���
	unsigned short uICName[32];	//���������
	long lICDis;				//����ھ���
}GuidanceHightWayInfo;

typedef struct tag_GuidanceJVInfo
{
	long lJVDis;	//JV����
}GuidanceJVInfo;

typedef struct tag_GuidanceLaneInfo
{
	long lNumOfLanes;	//��������
	char LaneStates[16];//������Ϣ,���λΪ�Ƿ�Ҫ����(��ʾ�Ƽ�����),������ʾ����
}GuidanceLaneInfo;


typedef struct tag_CatchImageInfo
{
	char strImagePixel[32];	//ͼƬ����˵��������800X480
	long lImageOffset;		//ƫ��
	long lImageSize;		//��С
}CatchImageInfo;

/*=============================================================
ת����Ϣ�ṹֵ����
================================================================*/
typedef struct tag_GUIDANCEINFO
{
	long lDrection; //ת�䷽��
	long lDistance; //�����յ���ľ��룬��λm(��·��)
	long lRemainDistance; //����Ŀ�ĵصľ��룬��λm
	long lTotalDistance; //��������Ŀ�ĵ�֮����ܾ��룬��λm
	long lRemainTime; //����Ŀ�ĵص�ʣ��ʱ��
	long lTotalTime; //��������Ŀ�ĵ�֮�����ʱ��
	char szCurrentRoadName[64]; //��ǰ��·����
	char szNextRoadName[64]; //��һ��·����
	long lCurrentRoadType; //��ǰ��·����
	long lCurrentSpeed; //��ǰ���٣���λkm/h
	long lCurrentLimitedSpeed;//��ǰ���Ƴ��٣���λkm/h
	long lCurrentGPSAngle; //��ǰGPS �Ƕȣ�������Ϊ��ʼ����˳ʱ����ת�ĽǶ�
	long lExitIndexRoads; //�����������
	long lNumOfOutRoads; //����������
	long lReserve; //����
}GUIDANCEINFO;


/*=============================================================
ת����Ϣ�����۽ṹ����
================================================================*/
typedef struct tag_CAMERAINFO
{
	short iType; //����������
	short iSpeed; //����������ֵ
	long  lDistance; //�����۾���
}CAMERAINFO;

#endif
