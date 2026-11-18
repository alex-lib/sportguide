import * as T from './three.module.js';
function roundedBox(w,h,d,r=.06){
 const s=new T.Shape();s.moveTo(-w/2+r,-h/2);s.lineTo(w/2-r,-h/2);s.quadraticCurveTo(w/2,-h/2,w/2,-h/2+r);s.lineTo(w/2,h/2-r);s.quadraticCurveTo(w/2,h/2,w/2-r,h/2);s.lineTo(-w/2+r,h/2);s.quadraticCurveTo(-w/2,h/2,-w/2,h/2-r);s.lineTo(-w/2,-h/2+r);s.quadraticCurveTo(-w/2,-h/2,-w/2+r,-h/2);
 const g=new T.ExtrudeGeometry(s,{depth:Math.max(.01,d-2*r),bevelEnabled:true,bevelSegments:3,steps:1,bevelSize:r*.42,bevelThickness:r,curveSegments:5});g.translate(0,0,-d/2+r);g.computeVertexNormals();return g;
}
function environment(renderer){
 const scene=new T.Scene();scene.background=new T.Color(.055,.055,.055);
 const box=(position,size,intensity)=>{const m=new T.Mesh(new T.PlaneGeometry(size[0],size[1]),new T.MeshBasicMaterial({color:new T.Color(intensity,intensity,intensity),side:T.DoubleSide}));m.position.fromArray(position);m.lookAt(0,0,0);scene.add(m);};
 box([-4,5,3],[3.5,7],5);box([5,2,1],[1.2,9],3.2);box([0,7,-3],[8,1.5],4.5);box([-2,-4,-3],[6,1.1],1.5);box([0,1,8],[2,5],.6);
 const gen=new T.PMREMGenerator(renderer);const target=gen.fromScene(scene,.06,.1,100);gen.dispose();scene.traverse((o)=>{o.geometry?.dispose();o.material?.dispose();});return target;
}

function cells(s){
 const nx=51,ny=39,n=nx*ny;const tiles=new T.InstancedMesh(roundedBox(.19,.19,.065,.016),s.dark,n);const mat=new T.MeshStandardMaterial({color:'#050505',roughness:.32,metalness:.25,emissive:'#7cd6bb'});
 mat.onBeforeCompile=shader=>{shader.uniforms.cellTime=s.time;shader.vertexShader='varying vec2 cellPosition;\n'+shader.vertexShader;shader.vertexShader=shader.vertexShader.replace('#include <begin_vertex>','#include <begin_vertex>\ncellPosition=instanceMatrix[3].xy;');shader.fragmentShader='varying vec2 cellPosition;uniform float cellTime;\n'+shader.fragmentShader;shader.fragmentShader=shader.fragmentShader.replace('#include <emissivemap_fragment>','#include <emissivemap_fragment>\nfloat phase=length(cellPosition-vec2(sin(cellTime*.2),cos(cellTime*.17)))*2.8-cellTime*1.5;float pulse=pow(.5+.5*sin(phase),10.);float second=pow(.5+.5*sin(cellPosition.x*2.+cellPosition.y*3.+cellTime*1.3),16.);totalEmissiveRadiance*=.012+pulse*.8+second*.27;');};
 const windows=new T.InstancedMesh(roundedBox(.13,.13,.012,.012),mat,n);const dummy=new T.Object3D();for(let y=0;y<ny;y++)for(let x=0;x<nx;x++){dummy.position.set((x-(nx-1)/2)*.23,(y-(ny-1)/2)*.23,-.025);dummy.updateMatrix();tiles.setMatrixAt(y*nx+x,dummy.matrix);dummy.position.z=.025;dummy.updateMatrix();windows.setMatrixAt(y*nx+x,dummy.matrix);}s.root.add(tiles,windows);s.aim(0,0,10.3);s.root.rotation.set(-.12,.10,-.04);return {width:nx*.23,height:ny*.23};
}

export { cells, environment };
