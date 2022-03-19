package br.edu.ifsp.scl.sdm.pa2.conceitoservice

import android.content.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.scl.sdm.pa2.conceitoservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val activityMainBinding: ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val lifeTimeServiceIntent: Intent by lazy{
        //Intent(this, LifeTimeStartedService::class.java)
        Intent(this, LifeTimeBoundService::class.java)

    }
    private lateinit var lifetimeBoundService: LifeTimeBoundService
    private var connected = false
    private val serviceConnection: ServiceConnection = object: ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            lifetimeBoundService = (binder as LifeTimeBoundService.LifetimeBoundServiceBinder).getService()
            connected = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            connected = false
        }

    }
    // classe usa handler para executar de segundo a segundo para atualizar o tempo, por meio de agendamento
    private inner class LifetimeServiceHandler(lifetimeServiceLooper: Looper): Handler(lifetimeServiceLooper){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (connected){
                runOnUiThread {
                    activityMainBinding.serviceLifeTimeTv.text =
                        lifetimeBoundService.lifetime.toString()
                }
                obtainMessage().also {
                    sendMessageDelayed(it, 1000)
                }
            }
        }
    }

    private lateinit var lifetimeServiceHandler: LifetimeServiceHandler


    /* BroadCastReceiver qe recebe o lifeTime do serviço, encaminhado pelo sendbroadcast*/
    /*private val receiveLifeTimeBr: BroadcastReceiver by lazy{
        object: BroadcastReceiver(){
            override fun onReceive(p0: Context?, intent: Intent?) {
                intent?.getIntExtra(EXTRA_LIFETIME, 0).also{ lifetime ->
                    //se o valor existe chama o also para executar sobre ele
                    activityMainBinding.serviceLifeTimeTv.text = lifetime.toString()
                }
            }

        }
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        HandlerThread("LifetimeHandlerThread").apply {
            start()
            lifetimeServiceHandler = LifetimeServiceHandler(looper)
        }

        with(activityMainBinding){
            iniciarServicoBt.setOnClickListener {
                //startService(lifeTimeServiceIntent)
                bindService(lifeTimeServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                lifetimeServiceHandler.obtainMessage().also {
                    lifetimeServiceHandler.sendMessageDelayed(it, 1000)
                }
            }
            finalizarServicoBt.setOnClickListener {
                //stopService(lifeTimeServiceIntent)
                unbindService(serviceConnection)
                connected = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //registerReceiver(receiveLifeTimeBr, IntentFilter("ACTION_RECEIVE_LIFETIME"))
    }

    override fun onStop() {
        super.onStop()
        //unregisterReceiver(receiveLifeTimeBr)
    }


}