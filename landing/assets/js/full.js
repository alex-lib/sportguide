import { DeltaScene } from './triangle-core.js';
import { startTriangleFallback } from './triangle-fallback.js';

const menu=document.querySelector('.menu-toggle');
const nav=document.querySelector('#main-nav');
function closeMenu(restoreFocus=false){
  nav.classList.remove('is-open');menu.setAttribute('aria-expanded','false');
  menu.setAttribute('aria-label','Открыть меню');if(restoreFocus)menu.focus();
}
menu.addEventListener('click',()=>{
  const expanded=menu.getAttribute('aria-expanded')!=='true';
  menu.setAttribute('aria-expanded',String(expanded));nav.classList.toggle('is-open',expanded);
  menu.setAttribute('aria-label',expanded?'Закрыть меню':'Открыть меню');
});
nav.addEventListener('click',event=>{if(event.target.closest('a'))closeMenu();});
document.addEventListener('keydown',event=>{if(event.key==='Escape'&&nav.classList.contains('is-open'))closeMenu(true);});
document.addEventListener('click',event=>{if(!nav.contains(event.target)&&!menu.contains(event.target))closeMenu();});
matchMedia('(min-width: 901px)').addEventListener('change',()=>closeMenu());

function startTriangle(){
  const host=document.querySelector('.triangle-scene');
  const cfg={faces:[
    {text:'sportguide',size:6,weight:'regular',fill:'#275e53',ink:'#e4efe5'},
    {text:'sportguide',size:5,weight:'bold',fill:'#ffad6e',ink:'#103b35'},
    {text:'sportguide',size:4,weight:'regular',fill:'#73bda1',ink:'#103b35'}
  ],font:{fontFamily:'Montserrat, Arial, sans-serif',fontWeight:600},thickness:4.2,roundness:3.6,shadow:0,hoverSpeed:5,speed:2.5,direction:'cw',drag:false,scale:150};
  try{
    const scene=new DeltaScene(host,cfg);
    const ro=new ResizeObserver(()=>scene.setSize(host.clientWidth,host.clientHeight));
    ro.observe(host);scene.setSize(host.clientWidth,host.clientHeight);scene.start();
    window.addEventListener('pagehide',()=>{ro.disconnect();scene.dispose();},{once:true});
  }catch(error){host.dataset.fallback='true';startTriangleFallback(host);}
}
startTriangle();
