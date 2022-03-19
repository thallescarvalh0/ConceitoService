package br.edu.ifsp.scl.sdm.pa2.conceitoservice

import android.app.Service
import android.content.Intent
import android.os.IBinder

class LifeTimeStartedService : Service() {
    /* Contador de segundos */
    private var lifetime: Int = 0

    companion object{
        /* Para passar o lifetime entre activity e service */
        val EXTRA_LIFETIME = "EXTRA_LIFETIME"
    }

    /* Nossa thread de trabalho que conta segundos em background -- Classe interna*/
    private inner class WorkerThread: Thread(){
        var running = false
        override fun run() {
            running = true
            while (running){
                sleep(1000)

                //Envia o lifetime para a Activity
                sendBroadcast(Intent("ACTION_RECEIVE_LIFETIME").also {
                    it.putExtra(EXTRA_LIFETIME, ++lifetime)
                })
            }

        }
    }
    private lateinit var workerThread: WorkerThread

    /* Primeira função execuada em qualuqer servico */
    override fun onCreate() {
        super.onCreate()
        workerThread = WorkerThread()
    }

    /* Só faz sentido se for serviço vinculado, senão retornar null*/
    override fun onBind(intent: Intent): IBinder? = null

    /* Chamado quando a Activity executa startService. Executa indefinidamente!!!!!
    * Executa até que seja chamado o método stopSelf(Serviço que chama) ou stopService(activity que chama)...*/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!workerThread.running){
            workerThread.start()
        }
        return START_STICKY //retorna parametro para quando faltar recurso e desejar reiniciar o servico

    }

    /* Última função executada. Apaga a luz e fecha a porta */

    override fun onDestroy() {
        super.onDestroy()
        workerThread.running = false
    }
}