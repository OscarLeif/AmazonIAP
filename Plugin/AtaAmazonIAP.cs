using JetBrains.Annotations;
using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class AtaAmazonIAP : MonoBehaviour
{
    #region Fields

    [CanBeNull]
    private static AtaAmazonIAP _instance;

    [NotNull]
    private static readonly object Lock = new object();

    [SerializeField]
    private bool _persistent = true;

    public static bool Quitting { get; private set; }

    #endregion

    #region Properties

    [NotNull]
    public static AtaAmazonIAP Instance
    {
        get
        {
            if (Quitting)
            {
                Debug.LogWarning(typeof(AtaAmazonIAP).Name + " Instance will not be returned because the application is quitting.");
                // ReSharper disable once AssignNullToNotNullAttribute
                return null;
            }
            lock (Lock)
            {
                if (_instance != null)
                    return _instance;
                var instances = FindObjectsOfType<AtaAmazonIAP>();
                var count = instances.Length;
                if (count > 0)
                {
                    if (count == 1)
                        return _instance = instances[0];
                    Debug.LogWarning(typeof(AtaAmazonIAP).Name + " There should never be more than one {nameof(Singleton)} of type {typeof(T)} in the scene, but {count} were found. The first instance found will be used, and all others will be destroyed.");
                    for (var i = 1; i < instances.Length; i++)
                        Destroy(instances[i]);
                    return _instance = instances[0];
                }

                Debug.Log(typeof(AtaAmazonIAP).Name + "An instance is needed in the scene and no existing instances were found, so a new instance will be created.");
                return _instance = new GameObject(typeof(AtaAmazonIAP).Name).AddComponent<AtaAmazonIAP>();
            }
        }
    }
    #endregion

    #region Monobegaviour Methods

    private void Awake()
    {
        if (_persistent)
        {
            DontDestroyOnLoad(this.gameObject);
            this.gameObject.name = "@AtaAmazonPurchaser";
        }        
    }

    private void Start()
    {
        this.Init();
    }

    private void OnApplicationQuit()
    {
        Quitting = true;
    }

    #endregion

    #region Ata Amazon Fields

    public bool PluginEnable = false;

    private AndroidJavaClass _javaClass;

    private AndroidJavaObject _javaObject { get {return _javaClass.GetStatic<AndroidJavaObject>("instance"); } }

    /// <summary>
    /// Java Callback change this variable
    /// </summary>
    private bool Initialize = false;
    #endregion

    #region Ata Amazon IAP Methods

    public void Init()
    {
        if (PluginEnable == false)
            return;
#if UNITY_ANDROID
        if(Application.platform == RuntimePlatform.Android)
        {
            _javaClass = new AndroidJavaClass("ata.games.amazon.iap.AmazonPurchaser");
            _javaClass.CallStatic("init", new AndroidPluginCallback(this));
        }
#endif
    }

    public void PurchaseEntitlement(String Sku)
    {
        if(PluginEnable && Initialize)
        {
            if(Application.platform == RuntimePlatform.Android)
            {
                _javaObject.Call("Purchase", Sku);
            }
        }
    }

    public bool CheckSkuOwned(String Sku)
    {
        bool isOwned = false;
        if(PluginEnable && Initialize)
        {
            if (Application.platform == RuntimePlatform.Android)
            {
                isOwned = _javaObject.Call<bool>("CheckSkuOwned", Sku);
            }
        }
        return isOwned;
    }
    #endregion

    #region ThreadDispatcher

    private static readonly Queue<Action> _executionQueue = new Queue<Action>();

    public static bool Exist()
    {
        return AtaAmazonIAP.Instance != null;
    }

    /// <summary>
    /// Used From Java Proxy Calls
    /// </summary>
    /// 
    public static AtaAmazonIAP _Instance()
    {
        if(!Exist())
        {
            throw new Exception("AtaAmazonIAP as MainThreadDispatcher could not find the AtaAmazonIap Object. Please make sure it exist");
        }
        return _instance;
    }

    private void Update()
    {
        lock (_executionQueue)
        {
            while (_executionQueue.Count > 0)
            {
                _executionQueue.Dequeue().Invoke();
            }
        }
    }

    /// <summary>
    /// Locks the queue and adds the IEnumerator to the queue
    /// </summary>
    /// <param name="action">IEnumerator function that will be executed from the main thread.</param>
    public void Enqueue(IEnumerator action)
    {
        lock (_executionQueue)
        {
            _executionQueue.Enqueue(() =>
            {
                StartCoroutine(action);
            });
        }
    }

    /// <summary>
    /// Locks the queue and adds the Action to the queue
    /// </summary>
    /// <param name="action">function that will be executed from the main thread.</param>
    public void Enqueue(Action action)
    {
        Enqueue(ActionWrapper(action));
    }

    IEnumerator ActionWrapper(Action a)
    {
        a();
        yield return null;
    }

    #endregion

    #region Callback Class

    public class AndroidPluginCallback: AndroidJavaProxy
    {
        private AtaAmazonIAP reference = null;

        public AndroidPluginCallback(AtaAmazonIAP reference): base ("ata.games.amazon.iap.AmazonIapCallback")
        {
            this.reference = reference;
        }

        public void onInitialize(bool isInit)
        {
            reference.Initialize = isInit;
            //reference.Enqueue(() => reference.AndroidShowToast("Plugin Initialize " + isInit));
        }
    }
    #endregion
}
